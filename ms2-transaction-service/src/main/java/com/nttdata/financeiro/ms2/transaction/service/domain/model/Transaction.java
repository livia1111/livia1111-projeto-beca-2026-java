package com.nttdata.financeiro.ms2.transaction.service.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class Transaction {

    private final UUID id;
    private final UUID userId;
    private final UUID targetUserId;
    private final TransactionType type;
    private final TransactionStatus status;
    private final BigDecimal amount;
    private final ExchangeRateSnapshot exchangeRate;
    private final String description;
    private final Category category;
    private final String rejectionReason;
    private final UUID originalTransactionId;
    private final Instant createdAt;

    private Transaction(UUID id, UUID userId, UUID targetUserId, TransactionType type,
                        TransactionStatus status, BigDecimal amount, ExchangeRateSnapshot exchangeRate,
                        String description, Category category, String rejectionReason,
                        UUID originalTransactionId, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.targetUserId = targetUserId;
        this.type = type;
        this.status = status;
        this.amount = amount;
        this.exchangeRate = exchangeRate;
        this.description = description;
        this.category = category;
        this.rejectionReason = rejectionReason;
        this.originalTransactionId = originalTransactionId;
        this.createdAt = createdAt;
    }

    /** Transacao aprovada: o snapshot USD/EUR e obrigatorio para atender toda consulta com conversao. */
    public static Transaction aprovada(UUID userId, UUID targetUserId, TransactionType type,
                                       BigDecimal amount, ExchangeRateSnapshot exchangeRate,
                                       String description, Category category,
                                       UUID originalTransactionId, Clock clock) {
        if (exchangeRate == null) {
            throw new IllegalArgumentException("A transação aprovada precisa da cotação de câmbio.");
        }
        validarInvariantes(userId, targetUserId, type, amount, category);
        return new Transaction(UUID.randomUUID(), userId, targetUserId, type,
                TransactionStatus.APROVADA, amount, exchangeRate,
                description, category, null, originalTransactionId, Instant.now(clock));
    }

    // Rejeitada sem cotação só quando a própria ausência de cotação causou a rejeição.
    public static Transaction rejeitada(UUID userId, UUID targetUserId, TransactionType type,
                                        BigDecimal amount, ExchangeRateSnapshot exchangeRate,
                                        String description, Category category,
                                        String rejectionReason, UUID originalTransactionId, Clock clock) {
        validarInvariantes(userId, targetUserId, type, amount, category);
        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new IllegalArgumentException("O motivo da rejeição é obrigatório.");
        }
        return new Transaction(UUID.randomUUID(), userId, targetUserId, type,
                TransactionStatus.REJEITADA, amount, exchangeRate,
                description, category, rejectionReason, originalTransactionId, Instant.now(clock));
    }

    private static void validarInvariantes(UUID userId, UUID targetUserId, TransactionType type,
                                           BigDecimal amount, Category category) {
        if (userId == null) throw new IllegalArgumentException("O usuário dono é obrigatório.");
        if (type == null) throw new IllegalArgumentException("O tipo da transação é obrigatório.");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }
        if (type == TransactionType.TRANSFERENCIA && targetUserId == null) {
            throw new IllegalArgumentException("O usuário destinatário é obrigatório em transferências.");
        }
        if (type == TransactionType.COMPRA && category == null) {
            throw new IllegalArgumentException("A categoria é obrigatória em compras.");
        }
    }

    // Preserva a identidade e a data recuperadas da persistência.
    public static Transaction reconstituir(UUID id, UUID userId, UUID targetUserId, TransactionType type,
                                           TransactionStatus status, BigDecimal amount,
                                           ExchangeRateSnapshot exchangeRate, String description,
                                           Category category, String rejectionReason,
                                           UUID originalTransactionId, Instant createdAt) {
        return new Transaction(id, userId, targetUserId, type, status, amount, exchangeRate,
                description, category, rejectionReason, originalTransactionId, createdAt);
    }

    // Conversao BRL -> moeda estrangeira usando a taxa de venda do snapshot.
    public record Conversion(TransactionCurrency currency, BigDecimal amount, BigDecimal rate) {}

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getTargetUserId() { return targetUserId; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
    public ExchangeRateSnapshot getExchangeRate() { return exchangeRate; }
    public boolean isConversionAvailable() { return exchangeRate != null; }

    public List<Conversion> getConversions() {
        if (exchangeRate == null) {
            return List.of();
        }
        return List.of(
                convert(TransactionCurrency.USD, exchangeRate.usdRate()),
                convert(TransactionCurrency.EUR, exchangeRate.eurRate())
        );
    }

    private Conversion convert(TransactionCurrency currency, BigDecimal rate) {
        BigDecimal converted = amount.divide(rate, 2, RoundingMode.HALF_UP);
        return new Conversion(currency, converted, rate);
    }

    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public String getRejectionReason() { return rejectionReason; }
    public UUID getOriginalTransactionId() { return originalTransactionId; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Transaction other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
