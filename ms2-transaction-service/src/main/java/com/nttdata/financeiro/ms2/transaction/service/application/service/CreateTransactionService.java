package com.nttdata.financeiro.ms2.transaction.service.application.service;

import com.nttdata.financeiro.ms2.transaction.service.application.exception.BusinessValidationException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.TargetUserNotFoundException;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.CreateTransactionCommand;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.CreateTransactionUseCase;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.TransactionRepository;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.UserServiceClient;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.Transaction;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionStatus;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;

public class CreateTransactionService implements CreateTransactionUseCase {

    private static final String MOTIVO_SALDO_INSUFICIENTE = "Saldo insuficiente.";

    private final TransactionRepository repository;
    private final UserServiceClient userService;
    private final Clock clock;

    public CreateTransactionService(TransactionRepository repository,
                                    UserServiceClient userService,
                                    Clock clock) {
        this.repository = repository;
        this.userService = userService;
        this.clock = clock;
    }

    @Override
    public Transaction criar(CreateTransactionCommand command) {
        validarEntrada(command);

        if (command.type() == TransactionType.TRANSFERENCIA) {
            if (!userService.usuarioExiste(command.targetUserId())) {
                throw new TargetUserNotFoundException();
            }
        }

        TransactionStatus status;
        String motivoRejeicao = null;

        if (command.type() == TransactionType.DEPOSITO) {
            status = TransactionStatus.APROVADA;
        } else {
            BigDecimal saldo = repository.calcularSaldo(command.userId());
            if (saldo.compareTo(command.amount()) < 0) {
                status = TransactionStatus.REJEITADA;
                motivoRejeicao = MOTIVO_SALDO_INSUFICIENTE;
            } else {
                status = TransactionStatus.APROVADA;
            }
        }

        Transaction transaction = Transaction.criar(
                command.userId(),
                command.targetUserId(),
                command.type(),
                status,
                command.amount(),
                command.description(),
                command.category(),
                motivoRejeicao,
                command.originalTransactionId(),
                clock
        );

        return repository.salvar(transaction);
    }

    private void validarEntrada(CreateTransactionCommand command) {
        if (command.type() == null) {
            throw new BusinessValidationException("O tipo da transação é obrigatório.");
        }
        if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("O valor deve ser maior que zero.");
        }
        if (command.type() == TransactionType.TRANSFERENCIA && command.targetUserId() == null) {
            throw new BusinessValidationException("O usuário destinatário é obrigatório em transferências.");
        }
        if (command.type() == TransactionType.COMPRA
                && (command.category() == null || command.category().isBlank())) {
            throw new BusinessValidationException("A categoria é obrigatória em compras.");
        }
    }
}