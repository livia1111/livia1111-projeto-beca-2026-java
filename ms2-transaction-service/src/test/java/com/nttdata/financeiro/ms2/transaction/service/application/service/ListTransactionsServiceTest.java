package com.nttdata.financeiro.ms2.transaction.service.application.service;

import com.nttdata.financeiro.ms2.transaction.service.application.exception.BusinessValidationException;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.TransactionRepository;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.Category;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.Transaction;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionStatus;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListTransactionsServiceTest {

    private TransactionRepository repository;
    private ListTransactionsService service;

    @BeforeEach
    void setUp() {
        repository = mock(TransactionRepository.class);
        service = new ListTransactionsService(repository);
    }

    @Test
    void delegaFiltrosAoRepositorio() {
        UUID userId = UUID.randomUUID();
        Instant inicio = Instant.parse("2026-11-01T00:00:00Z");
        Instant fim = Instant.parse("2026-11-30T23:59:59Z");
        TransactionType tipo = TransactionType.COMPRA;
        Transaction transaction = Transaction.reconstituir(UUID.randomUUID(), userId, null,
                tipo, TransactionStatus.APROVADA, new BigDecimal("50"),
                null, "teste", Category.LAZER, null, null, Instant.now());

        when(repository.listar(userId, inicio, fim, tipo)).thenReturn(List.of(transaction));

        List<Transaction> result = service.listar(userId, inicio, fim, tipo);

        assertEquals(1, result.size());
        verify(repository).listar(userId, inicio, fim, tipo);
    }

    @Test
    void filtrosNulosPassamComoNulos() {
        UUID userId = UUID.randomUUID();
        when(repository.listar(userId, null, null, null)).thenReturn(List.of());

        List<Transaction> result = service.listar(userId, null, null, null);

        assertTrue(result.isEmpty());
        verify(repository).listar(userId, null, null, null);
    }

    @Test
    void periodoInicialPosteriorAoFinalRejeitadoNoCasoDeUso() {
        UUID userId = UUID.randomUUID();
        Instant inicio = Instant.parse("2026-11-30T00:00:00Z");
        Instant fim = Instant.parse("2026-11-01T00:00:00Z");

        assertThrows(BusinessValidationException.class,
                () -> service.listar(userId, inicio, fim, null));
        verify(repository, never()).listar(any(), any(), any(), any());
    }
}