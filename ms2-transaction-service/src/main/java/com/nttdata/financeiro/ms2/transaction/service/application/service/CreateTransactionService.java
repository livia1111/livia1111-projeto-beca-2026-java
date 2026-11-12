package com.nttdata.financeiro.ms2.transaction.service.application.service;

import com.nttdata.financeiro.ms2.transaction.service.application.exception.AccountServiceUnavailableException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.AccountNotFoundException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.BusinessValidationException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.TargetUserNotFoundException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.TransactionNotFoundException;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.CreateTransactionCommand;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.CreateTransactionUseCase;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.AccountServiceClient;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.ExchangeRateClient;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.TransactionRepository;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.UserServiceClient;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.AccountBalance;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.ExchangeRateSnapshot;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.Transaction;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionStatus;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionType;
import com.nttdata.financeiro.ms2.transaction.service.domain.service.TransactionApprovalPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class CreateTransactionService implements CreateTransactionUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(CreateTransactionService.class);
    private static final ZoneId ZONA_NEGOCIO = ZoneId.of("America/Sao_Paulo");

    static final String MOTIVO_COTACAO_INDISPONIVEL = "Cotação de câmbio indisponível.";
    static final String MOTIVO_SALDO_INDISPONIVEL = "Serviço de saldo indisponível.";
    static final String MOTIVO_CONTA_NAO_ENCONTRADA = "Conta não encontrada no serviço de saldo.";
    static final String MOTIVO_CONTA_DESTINO_NAO_ENCONTRADA =
            "Conta do destinatário não encontrada no serviço de saldo.";
    static final String MOTIVO_SALDO_DESTINO_INDISPONIVEL =
            "Serviço de saldo do destinatário indisponível.";

    private final TransactionRepository repository;
    private final UserServiceClient userService;
    private final AccountServiceClient accountService;
    private final ExchangeRateClient exchangeRateClient;
    private final TransactionApprovalPolicy approvalPolicy;
    private final Clock clock;

    public CreateTransactionService(TransactionRepository repository,
                                    UserServiceClient userService,
                                    AccountServiceClient accountService,
                                    ExchangeRateClient exchangeRateClient,
                                    TransactionApprovalPolicy approvalPolicy,
                                    Clock clock) {
        this.repository = repository;
        this.userService = userService;
        this.accountService = accountService;
        this.exchangeRateClient = exchangeRateClient;
        this.approvalPolicy = approvalPolicy;
        this.clock = clock;
    }

    @Override
    public Transaction criar(CreateTransactionCommand command) {
        validarEntrada(command);
        if (command.type() == TransactionType.TRANSFERENCIA
                && !userService.usuarioExiste(command.targetUserId())) {
            throw new TargetUserNotFoundException();
        }
        validarOriginal(command);

        // Cotação: tenta a data de hoje (BrasilAPI já resolve fim de semana/feriado)
        // e, se falhar, a data de ontem. Sem nenhuma cotação a transação é rejeitada.
        LocalDate hoje = LocalDate.now(clock.withZone(ZONA_NEGOCIO));
        ExchangeRateSnapshot snapshot = exchangeRateClient.consultar(hoje)
                .or(() -> exchangeRateClient.consultar(hoje.minusDays(1)))
                .orElse(null);
        if (snapshot == null) {
            return rejeitar(command, null, MOTIVO_COTACAO_INDISPONIVEL);
        }

        // Depósito não depende de saldo/limites; os demais tipos consultam a MockAPI.
        if (command.type() == TransactionType.DEPOSITO) {
            return gravar(command, snapshot, null);
        }

        AccountBalance account;
        try {
            account = accountService.consultar(command.userId());
            validarPosicaoDaConta(account, command.userId());
        } catch (AccountNotFoundException ex) {
            return rejeitar(command, snapshot, MOTIVO_CONTA_NAO_ENCONTRADA);
        } catch (AccountServiceUnavailableException ex) {
            return rejeitar(command, snapshot, MOTIVO_SALDO_INDISPONIVEL);
        }

        if (command.type() == TransactionType.TRANSFERENCIA) {
            try {
                AccountBalance targetAccount = accountService.consultar(command.targetUserId());
                validarPosicaoDaConta(targetAccount, command.targetUserId());
            } catch (AccountNotFoundException ex) {
                return rejeitar(command, snapshot, MOTIVO_CONTA_DESTINO_NAO_ENCONTRADA);
            } catch (AccountServiceUnavailableException ex) {
                return rejeitar(command, snapshot, MOTIVO_SALDO_DESTINO_INDISPONIVEL);
            }
        }

        LocalDate today = LocalDate.now(clock.withZone(ZONA_NEGOCIO));
        Instant dayStart = today.atStartOfDay(ZONA_NEGOCIO).toInstant();
        Instant nextDayStart = today.plusDays(1).atStartOfDay(ZONA_NEGOCIO).toInstant();
        BigDecimal spentToday = repository.calcularSaidasAprovadas(
                command.userId(), dayStart, nextDayStart);
        String rejectionReason = approvalPolicy.avaliar(command.amount(), account,
                spentToday == null ? BigDecimal.ZERO : spentToday).orElse(null);

        return gravar(command, snapshot, rejectionReason);
    }

    private Transaction gravar(CreateTransactionCommand command, ExchangeRateSnapshot snapshot,
                               String rejectionReason) {
        Transaction transaction = rejectionReason == null
                ? Transaction.aprovada(command.userId(), command.targetUserId(), command.type(),
                    command.amount(), snapshot, command.description(), command.category(),
                    command.originalTransactionId(), clock)
                : Transaction.rejeitada(command.userId(), command.targetUserId(), command.type(),
                    command.amount(), snapshot, command.description(), command.category(),
                    rejectionReason, command.originalTransactionId(), clock);
        Transaction saved = repository.salvar(transaction);
        atualizarSaldoExterno(saved);
        return saved;
    }

    private Transaction rejeitar(CreateTransactionCommand command, ExchangeRateSnapshot snapshot,
                                 String rejectionReason) {
        Transaction rejected = Transaction.rejeitada(command.userId(), command.targetUserId(),
                command.type(), command.amount(), snapshot, command.description(),
                command.category(), rejectionReason, command.originalTransactionId(), clock);
        return repository.salvar(rejected);
    }

    private void validarEntrada(CreateTransactionCommand command) {
        if (command == null || command.userId() == null || command.type() == null) {
            throw new BusinessValidationException("Usuário e tipo são obrigatórios.");
        }
        if (command.amount() == null || command.amount().signum() <= 0) {
            throw new BusinessValidationException("O valor deve ser maior que zero.");
        }
        if (command.type() == TransactionType.TRANSFERENCIA && command.targetUserId() == null) {
            throw new BusinessValidationException("O usuário destinatário é obrigatório em transferências.");
        }
        if (command.type() == TransactionType.COMPRA
                && command.category() == null) {
            throw new BusinessValidationException("A categoria é obrigatória em compras.");
        }
    }

    // Correção: a transação original nunca é apagada; a nova apenas aponta para ela.
    private void validarOriginal(CreateTransactionCommand command) {
        if (command.originalTransactionId() == null) {
            return;
        }
        Transaction original = repository.buscarPorId(command.originalTransactionId())
                .orElseThrow(TransactionNotFoundException::new);
        if (!command.userId().equals(original.getUserId())) {
            throw new TransactionNotFoundException();
        }
    }

    private void validarPosicaoDaConta(AccountBalance account, java.util.UUID expectedUserId) {
        if (account == null || !expectedUserId.equals(account.userId())
                || account.balance() == null || account.dailyLimit() == null
                || account.perTransactionLimit() == null || account.balance().signum() < 0
                || account.dailyLimit().signum() < 0 || account.perTransactionLimit().signum() < 0) {
            throw new AccountServiceUnavailableException();
        }
    }

    /** Atualiza a MockAPI somente após persistir uma transação aprovada, como projeção do saldo atual. */
    private void atualizarSaldoExterno(Transaction transaction) {
        if (transaction.getStatus() != TransactionStatus.APROVADA) return;

        BigDecimal amount = transaction.getAmount();
        try {
            switch (transaction.getType()) {
                case DEPOSITO -> accountService.ajustarSaldo(transaction.getUserId(), amount);
                case RETIRADA, COMPRA -> accountService.ajustarSaldo(
                        transaction.getUserId(), amount.negate());
                case TRANSFERENCIA -> {
                    accountService.ajustarSaldo(transaction.getUserId(), amount.negate());
                    accountService.ajustarSaldo(transaction.getTargetUserId(), amount);
                }
            }
        } catch (AccountNotFoundException | AccountServiceUnavailableException ex) {
            // A transação já foi gravada; mantém o histórico e registra a falha da projeção externa.
            LOG.warn("Saldo do MockAPI não atualizado para a transação {}: {}",
                    transaction.getId(), ex.getMessage());
        }
    }
}
