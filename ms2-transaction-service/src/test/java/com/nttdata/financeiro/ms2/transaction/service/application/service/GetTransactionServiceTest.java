package com.nttdata.financeiro.ms2.transaction.service.application.service;

import com.nttdata.financeiro.ms2.transaction.service.application.exception.AccessDeniedToTransactionException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.TransactionNotFoundException;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.TransactionRepository;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.Transaction;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionStatus;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetTransactionServiceTest {

    private TransactionRepository repository;
    private GetTransactionService service;

    @BeforeEach
    void setUp() {
        repository = mock(TransactionRepository.class);
        service = new GetTransactionService(repository);
    }

    @Test
    void donoEncontraTransacao() {
        UUID ownerId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        Transaction transaction = Transaction.reconstituir(transactionId, ownerId, null,
                TransactionType.DEPOSITO, TransactionStatus.APROVADA,
                new BigDecimal("100"), "teste", null, null, null, Instant.now());

        when(repository.buscarPorId(transactionId)).thenReturn(Optional.of(transaction));

        Transaction result = service.buscar(transactionId, ownerId);

        assertEquals(transactionId, result.getId());
        assertEquals(ownerId, result.getUserId());
    }

    @Test
    void naoEncontradaLanca404() {
        UUID transactionId = UUID.randomUUID();
        when(repository.buscarPorId(transactionId)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> service.buscar(transactionId, UUID.randomUUID()));
    }

    @Test
    void outroUsuarioLancaAcessoNegado() {
        UUID ownerId = UUID.randomUUID();
        UUID outroId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        Transaction transaction = Transaction.reconstituir(transactionId, ownerId, null,
                TransactionType.DEPOSITO, TransactionStatus.APROVADA,
                new BigDecimal("100"), "teste", null, null, null, Instant.now());

        when(repository.buscarPorId(transactionId)).thenReturn(Optional.of(transaction));

        assertThrows(AccessDeniedToTransactionException.class,
                () -> service.buscar(transactionId, outroId));
    }
}