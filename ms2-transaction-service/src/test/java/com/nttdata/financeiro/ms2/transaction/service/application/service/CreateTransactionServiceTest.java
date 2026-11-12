package com.nttdata.financeiro.ms2.transaction.service.application.service;

import com.nttdata.financeiro.ms2.transaction.service.application.exception.AccountNotFoundException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.AccountServiceUnavailableException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.BusinessValidationException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.TargetUserNotFoundException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.TransactionNotFoundException;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.CreateTransactionCommand;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.AccountServiceClient;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.ExchangeRateClient;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.TransactionRepository;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.UserServiceClient;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.AccountBalance;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.Category;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.ExchangeRateSnapshot;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.Transaction;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionStatus;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionType;
import com.nttdata.financeiro.ms2.transaction.service.domain.service.TransactionApprovalPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateTransactionServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 10);
    private static final LocalDate ONTEM = LocalDate.of(2026, 11, 9);
    private static final ExchangeRateSnapshot SNAPSHOT =
            new ExchangeRateSnapshot(HOJE, new BigDecimal("5.00"), new BigDecimal("5.50"));

    private TransactionRepository repository;
    private UserServiceClient userService;
    private AccountServiceClient accountService;
    private ExchangeRateClient exchangeRateClient;
    private CreateTransactionService service;
    private Clock clock;

    @BeforeEach
    void setUp() {
        repository = mock(TransactionRepository.class);
        userService = mock(UserServiceClient.class);
        accountService = mock(AccountServiceClient.class);
        exchangeRateClient = mock(ExchangeRateClient.class);
        clock = Clock.fixed(Instant.parse("2026-11-10T10:00:00Z"), ZoneOffset.UTC);
        service = new CreateTransactionService(repository, userService, accountService,
                exchangeRateClient, new TransactionApprovalPolicy(), clock);
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(exchangeRateClient.consultar(any())).thenReturn(Optional.of(SNAPSHOT));
        when(repository.calcularSaidasAprovadas(any(), any(), any())).thenReturn(BigDecimal.ZERO);
    }

    @Test
    void depositoAprovadoSemConsultarContaParaAprovacao() {
        UUID userId = UUID.randomUUID();
        Transaction result = service.criar(command(userId, null, TransactionType.DEPOSITO,
                "100.00", null, null));

        assertEquals(TransactionStatus.APROVADA, result.getStatus());
        assertNull(result.getRejectionReason());
        assertNotNull(result.getExchangeRate());
        verify(accountService, never()).consultar(any());
        verify(accountService).ajustarSaldo(userId, new BigDecimal("100.00"));
        verify(repository).salvar(any());
    }

    @Test
    void saqueComSaldoSuficienteAprovadoEDebitaMock() {
        UUID userId = UUID.randomUUID();
        when(accountService.consultar(userId)).thenReturn(account(userId, "500.00", "500.00", "200.00"));

        Transaction result = service.criar(command(userId, null, TransactionType.RETIRADA,
                "100.00", null, null));

        assertEquals(TransactionStatus.APROVADA, result.getStatus());
        verify(accountService).ajustarSaldo(userId, new BigDecimal("-100.00"));
    }

    @Test
    void saqueAcimaDoSaldoFicaRejeitadoGravadoENaoAlteraMock() {
        UUID userId = UUID.randomUUID();
        when(accountService.consultar(userId)).thenReturn(account(userId, "50.00", "500.00", "200.00"));

        Transaction result = service.criar(command(userId, null, TransactionType.RETIRADA,
                "100.00", null, null));

        assertEquals(TransactionStatus.REJEITADA, result.getStatus());
        assertEquals("Saldo insuficiente.", result.getRejectionReason());
        assertNotNull(result.getExchangeRate());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(repository).salvar(captor.capture());
        assertEquals(TransactionStatus.REJEITADA, captor.getValue().getStatus());
        verify(accountService, never()).ajustarSaldo(any(), any());
    }

    @Test
    void valorAcimaDoLimitePorTransacaoRejeitado() {
        UUID userId = UUID.randomUUID();
        when(accountService.consultar(userId)).thenReturn(account(userId, "500.00", "500.00", "50.00"));

        Transaction result = service.criar(command(userId, null, TransactionType.RETIRADA,
                "100.00", null, null));

        assertEquals(TransactionStatus.REJEITADA, result.getStatus());
        assertEquals("Valor excede o limite por transação.", result.getRejectionReason());
        verify(accountService, never()).ajustarSaldo(any(), any());
    }

    @Test
    void somaDoDiaAcimaDoLimiteDiarioRejeitado() {
        UUID userId = UUID.randomUUID();
        when(accountService.consultar(userId)).thenReturn(account(userId, "500.00", "200.00", "500.00"));
        when(repository.calcularSaidasAprovadas(any(), any(), any())).thenReturn(new BigDecimal("150.00"));

        Transaction result = service.criar(command(userId, null, TransactionType.RETIRADA,
                "100.00", null, null));

        assertEquals(TransactionStatus.REJEITADA, result.getStatus());
        assertEquals("Valor excede o limite diário.", result.getRejectionReason());
    }

    @Test
    void transferenciaParaUsuarioInexistenteNaoGrava() {
        UUID userId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        when(userService.usuarioExiste(targetId)).thenReturn(false);

        assertThrows(TargetUserNotFoundException.class, () -> service.criar(
                command(userId, targetId, TransactionType.TRANSFERENCIA, "100.00", null, null)));
        verify(repository, never()).salvar(any());
    }

    @Test
    void transferenciaAprovadaDebitaOrigemECreditaDestinoNoMock() {
        UUID userId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        when(userService.usuarioExiste(targetId)).thenReturn(true);
        when(accountService.consultar(userId)).thenReturn(account(userId, "500.00", "500.00", "200.00"));
        when(accountService.consultar(targetId)).thenReturn(account(targetId, "0.00", "500.00", "200.00"));

        Transaction result = service.criar(command(userId, targetId, TransactionType.TRANSFERENCIA,
                "100.00", null, null));

        assertEquals(TransactionStatus.APROVADA, result.getStatus());
        verify(accountService).ajustarSaldo(userId, new BigDecimal("-100.00"));
        verify(accountService).ajustarSaldo(targetId, new BigDecimal("100.00"));
    }

    @Test
    void compraSemCategoriaERejeitadaComoRequisicaoInvalida() {
        UUID userId = UUID.randomUUID();
        assertThrows(BusinessValidationException.class, () -> service.criar(
                command(userId, null, TransactionType.COMPRA, "100.00", null, null)));
        verify(repository, never()).salvar(any());
    }

    @Test
    void valorZeroERejeitado() {
        UUID userId = UUID.randomUUID();
        assertThrows(BusinessValidationException.class, () -> service.criar(
                command(userId, null, TransactionType.DEPOSITO, "0.00", null, null)));
        verify(repository, never()).salvar(any());
    }

    @Test
    void mockApiIndisponivelGravaRejeitadaEMantemHistorico() {
        UUID userId = UUID.randomUUID();
        when(accountService.consultar(userId)).thenThrow(new AccountServiceUnavailableException());

        Transaction result = service.criar(command(userId, null, TransactionType.RETIRADA,
                "100.00", null, null));

        assertEquals(TransactionStatus.REJEITADA, result.getStatus());
        assertEquals("Serviço de saldo indisponível.", result.getRejectionReason());
        assertNotNull(result.getExchangeRate());
        verify(accountService, never()).ajustarSaldo(any(), any());
    }

    @Test
    void contaInexistenteNaMockApiGravaRejeitada() {
        UUID userId = UUID.randomUUID();
        when(accountService.consultar(userId)).thenThrow(new AccountNotFoundException());

        Transaction result = service.criar(command(userId, null, TransactionType.RETIRADA,
                "100.00", null, null));

        assertEquals(TransactionStatus.REJEITADA, result.getStatus());
        assertEquals("Conta não encontrada no serviço de saldo.", result.getRejectionReason());
    }

    @Test
    void cotacaoIndisponivelHojeEOntemGravaRejeitadaSemSnapshot() {
        UUID userId = UUID.randomUUID();
        when(exchangeRateClient.consultar(any())).thenReturn(Optional.empty());

        Transaction result = service.criar(command(userId, null, TransactionType.DEPOSITO,
                "100.00", null, null));

        assertEquals(TransactionStatus.REJEITADA, result.getStatus());
        assertEquals("Cotação de câmbio indisponível.", result.getRejectionReason());
        assertNull(result.getExchangeRate());
        verify(exchangeRateClient).consultar(HOJE);
        verify(exchangeRateClient).consultar(ONTEM);
        verify(accountService, never()).consultar(any());
        verify(accountService, never()).ajustarSaldo(any(), any());
    }

    @Test
    void cotacaoFalhaHojeMasFuncionaOntemUsaDataDeOntem() {
        UUID userId = UUID.randomUUID();
        ExchangeRateSnapshot deOntem = new ExchangeRateSnapshot(ONTEM,
                new BigDecimal("4.90"), new BigDecimal("5.40"));
        when(exchangeRateClient.consultar(HOJE)).thenReturn(Optional.empty());
        when(exchangeRateClient.consultar(ONTEM)).thenReturn(Optional.of(deOntem));

        Transaction result = service.criar(command(userId, null, TransactionType.DEPOSITO,
                "100.00", null, null));

        assertEquals(TransactionStatus.APROVADA, result.getStatus());
        assertEquals(deOntem, result.getExchangeRate());
        verify(exchangeRateClient).consultar(HOJE);
        verify(exchangeRateClient).consultar(ONTEM);
    }

    @Test
    void originalInexistenteNaoGravaNemConsultaCotacao() {
        UUID userId = UUID.randomUUID();
        UUID originalId = UUID.randomUUID();
        when(repository.buscarPorId(originalId)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> service.criar(
                command(userId, null, TransactionType.DEPOSITO, "100.00", null, originalId)));
        verify(repository, never()).salvar(any());
        verify(exchangeRateClient, never()).consultar(any());
    }

    @Test
    void originalDeOutroUsuarioNaoGrava() {
        UUID userId = UUID.randomUUID();
        UUID originalId = UUID.randomUUID();
        Transaction original = Transaction.reconstituir(originalId, UUID.randomUUID(), null,
                TransactionType.DEPOSITO, TransactionStatus.APROVADA, new BigDecimal("10.00"),
                SNAPSHOT, null, null, null, null, Instant.now());
        when(repository.buscarPorId(originalId)).thenReturn(Optional.of(original));

        assertThrows(TransactionNotFoundException.class, () -> service.criar(
                command(userId, null, TransactionType.DEPOSITO, "100.00", null, originalId)));
        verify(repository, never()).salvar(any());
    }

    @Test
    void originalDoProprioUsuarioPermiteCorrecao() {
        UUID userId = UUID.randomUUID();
        UUID originalId = UUID.randomUUID();
        Transaction original = Transaction.reconstituir(originalId, userId, null,
                TransactionType.DEPOSITO, TransactionStatus.REJEITADA, new BigDecimal("10.00"),
                SNAPSHOT, null, null, "Saldo insuficiente.", null, Instant.now());
        when(repository.buscarPorId(originalId)).thenReturn(Optional.of(original));

        Transaction result = service.criar(command(userId, null, TransactionType.DEPOSITO,
                "100.00", null, originalId));

        assertEquals(TransactionStatus.APROVADA, result.getStatus());
        assertEquals(originalId, result.getOriginalTransactionId());
    }

    private CreateTransactionCommand command(UUID userId, UUID targetUserId, TransactionType type,
                                             String amount, Category category, UUID originalId) {
        return new CreateTransactionCommand(userId, targetUserId, type,
                new BigDecimal(amount), "teste", category, originalId);
    }

    private AccountBalance account(UUID userId, String balance, String dailyLimit, String perTransactionLimit) {
        return new AccountBalance(userId, new BigDecimal(balance),
                new BigDecimal(dailyLimit), new BigDecimal(perTransactionLimit));
    }
}
