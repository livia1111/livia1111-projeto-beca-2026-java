package com.nttdata.financeiro.ms2.transaction.service.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-10T10:00:00Z"), ZoneOffset.UTC);
    private static final ExchangeRateSnapshot SNAPSHOT =
            new ExchangeRateSnapshot(LocalDate.of(2026, 11, 10), new BigDecimal("5.00"), new BigDecimal("5.50"));

    @Test
    void aprovadaSemSnapshotLanca() {
        assertThrows(IllegalArgumentException.class, () -> Transaction.aprovada(
                UUID.randomUUID(), null, TransactionType.DEPOSITO, new BigDecimal("100.00"),
                null, null, null, null, CLOCK));
    }

    @Test
    void aprovadaComSnapshotOkPreservaCampos() {
        UUID userId = UUID.randomUUID();
        Transaction t = Transaction.aprovada(userId, null, TransactionType.DEPOSITO,
                new BigDecimal("100.00"), SNAPSHOT, "pix", null, null, CLOCK);

        assertEquals(TransactionStatus.APROVADA, t.getStatus());
        assertNotNull(t.getId());
        assertEquals(SNAPSHOT, t.getExchangeRate());
        assertTrue(t.isConversionAvailable());
        assertNotNull(t.getCreatedAt());
        assertNull(t.getRejectionReason());
    }

    @Test
    void rejeitadaExigeMotivo() {
        assertThrows(IllegalArgumentException.class, () -> Transaction.rejeitada(
                UUID.randomUUID(), null, TransactionType.DEPOSITO, new BigDecimal("100.00"),
                SNAPSHOT, null, null, "  ", null, CLOCK));
    }

    @Test
    void rejeitadaSemSnapshotPermitidaSomentePorAusenciaDeCotacao() {
        Transaction t = Transaction.rejeitada(UUID.randomUUID(), null, TransactionType.DEPOSITO,
                new BigDecimal("100.00"), null, null, null,
                "Cotação de câmbio indisponível.", null, CLOCK);

        assertEquals(TransactionStatus.REJEITADA, t.getStatus());
        assertFalse(t.isConversionAvailable());
        assertTrue(t.getConversions().isEmpty());
    }

    @Test
    void compraSemCategoriaLanca() {
        assertThrows(IllegalArgumentException.class, () -> Transaction.aprovada(
                UUID.randomUUID(), null, TransactionType.COMPRA, new BigDecimal("100.00"),
                SNAPSHOT, null, null, null, CLOCK));
    }

    @Test
    void transferenciaSemDestinoLanca() {
        assertThrows(IllegalArgumentException.class, () -> Transaction.aprovada(
                UUID.randomUUID(), null, TransactionType.TRANSFERENCIA, new BigDecimal("100.00"),
                SNAPSHOT, null, null, null, CLOCK));
    }

    @Test
    void valorZerouNegativoLanca() {
        assertThrows(IllegalArgumentException.class, () -> Transaction.aprovada(
                UUID.randomUUID(), null, TransactionType.DEPOSITO, BigDecimal.ZERO,
                SNAPSHOT, null, null, null, CLOCK));
    }

    @Test
    void converteValorOriginalParaUsdEurComTaxaEData() {
        Transaction t = Transaction.aprovada(UUID.randomUUID(), null, TransactionType.DEPOSITO,
                new BigDecimal("100.00"), SNAPSHOT, null, null, null, CLOCK);

        List<Transaction.Conversion> conversions = t.getConversions();

        assertEquals(2, conversions.size());
        Transaction.Conversion usd = conversions.get(0);
        Transaction.Conversion eur = conversions.get(1);
        assertEquals(TransactionCurrency.USD, usd.currency());
        assertEquals(new BigDecimal("20.00"), usd.amount());
        assertEquals(new BigDecimal("5.00"), usd.rate());
        assertEquals(TransactionCurrency.EUR, eur.currency());
        assertEquals(new BigDecimal("18.18"), eur.amount());
        assertEquals(new BigDecimal("5.50"), eur.rate());
        assertEquals(LocalDate.of(2026, 11, 10), t.getExchangeRate().rateDate());
    }

    @Test
    void snapshotExigeDataTaxasPositivas() {
        assertThrows(IllegalArgumentException.class,
                () -> new ExchangeRateSnapshot(null, new BigDecimal("5.00"), new BigDecimal("5.50")));
        assertThrows(IllegalArgumentException.class,
                () -> new ExchangeRateSnapshot(LocalDate.now(), null, new BigDecimal("5.50")));
        assertThrows(IllegalArgumentException.class,
                () -> new ExchangeRateSnapshot(LocalDate.now(), new BigDecimal("5.00"), BigDecimal.ZERO));
    }

    @Test
    void reconstituirPreservaIdentidadeEData() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-11-01T12:00:00Z");

        Transaction t = Transaction.reconstituir(id, userId, null, TransactionType.DEPOSITO,
                TransactionStatus.APROVADA, new BigDecimal("100.00"), SNAPSHOT,
                "desc", null, null, null, createdAt);

        assertEquals(id, t.getId());
        assertEquals(userId, t.getUserId());
        assertEquals(createdAt, t.getCreatedAt());
        assertEquals(SNAPSHOT, t.getExchangeRate());
    }
}
