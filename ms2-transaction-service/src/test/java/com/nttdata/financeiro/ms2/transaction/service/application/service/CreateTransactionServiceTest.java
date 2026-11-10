package com.nttdata.financeiro.ms2.transaction.service.application.service;

import com.nttdata.financeiro.ms2.transaction.service.application.exception.BusinessValidationException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.TargetUserNotFoundException;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.CreateTransactionCommand;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.TransactionRepository;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.UserServiceClient;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.Transaction;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionStatus;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateTransactionServiceTest {

    private TransactionRepository repository;
    private UserServiceClient userService;
    private CreateTransactionService service;
    private Clock clock;

    @BeforeEach
    void setUp() {
        repository = mock(TransactionRepository.class);
        userService = mock(UserServiceClient.class);
        clock = Clock.fixed(Instant.parse("2026-11-10T10:00:00Z"), ZoneOffset.UTC);
        service = new CreateTransactionService(repository, userService, clock);
        when(repository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void depositoSempreAprovado() {
        UUID userId = UUID.randomUUID();
        var command = new CreateTransactionCommand(userId, null, TransactionType.DEPOSITO,
                new BigDecimal("100.00"), "teste", null, null);

        Transaction result = service.criar(command);

        assertEquals(TransactionStatus.APROVADA, result.getStatus());
        assertNull(result.getRejectionReason());
        verify(repository).salvar(any());
    }

    @Test
    void saqueComSaldoSuficienteAprovado() {
        UUID userId = UUID.randomUUID();
        when(repository.calcularSaldo(userId)).thenReturn(new BigDecimal("500.00"));
        var command = new CreateTransactionCommand(userId, null, TransactionType.RETIRADA,
                new BigDecimal("100.00"), "teste", null, null);

        Transaction result = service.criar(command);

        assertEquals(TransactionStatus.APROVADA, result.getStatus());
    }

    @Test
    void saqueSemSaldoFicaRejeitadoComMotivo() {
        UUID userId = UUID.randomUUID();
        when(repository.calcularSaldo(userId)).thenReturn(new BigDecimal("50.00"));
        var command = new CreateTransactionCommand(userId, null, TransactionType.RETIRADA,
                new BigDecimal("100.00"), "teste", null, null);

        Transaction result = service.criar(command);

        assertEquals(TransactionStatus.REJEITADA, result.getStatus());
        assertEquals("Saldo insuficiente.", result.getRejectionReason());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(repository).salvar(captor.capture());
        assertEquals(TransactionStatus.REJEITADA, captor.getValue().getStatus());
    }

    @Test
    void transferenciaParaUsuarioInexistenteLanca404() {
        UUID userId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        when(userService.usuarioExiste(targetId)).thenReturn(false);
        var command = new CreateTransactionCommand(userId, targetId, TransactionType.TRANSFERENCIA,
                new BigDecimal("100.00"), "teste", null, null);

        assertThrows(TargetUserNotFoundException.class, () -> service.criar(command));
        verify(repository, never()).salvar(any());
    }

    @Test
    void compraSemCategoriaLanca400() {
        UUID userId = UUID.randomUUID();
        var command = new CreateTransactionCommand(userId, null, TransactionType.COMPRA,
                new BigDecimal("100.00"), "teste", null, null);

        assertThrows(BusinessValidationException.class, () -> service.criar(command));
    }

    @Test
    void valorZeroLanca400() {
        UUID userId = UUID.randomUUID();
        var command = new CreateTransactionCommand(userId, null, TransactionType.DEPOSITO,
                BigDecimal.ZERO, "teste", null, null);

        assertThrows(BusinessValidationException.class, () -> service.criar(command));
    }
}