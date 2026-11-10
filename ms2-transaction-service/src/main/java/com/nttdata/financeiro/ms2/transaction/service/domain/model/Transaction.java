package com.nttdata.financeiro.ms2.transaction.service.domain.model;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/** Modelo de dados com fabricas explicitas para criacao e reconstituicao. */
public final class Transaction {

    private final UUID id;
    private final UUID userId;
    private final UUID targetUserId;
    private final TransactionType type;
    private final TransactionStatus status;
    private final BigDecimal amount;
    private final String description;
    private final String category;
    private final String rejectionReason;
    private final UUID originalTransactionId;
    private final Instant createdAt;

    private Transaction(UUID id, UUID userId, UUID targetUserId, TransactionType type,
                        TransactionStatus status, BigDecimal amount, String description,
                        String category, String rejectionReason, UUID originalTransactionId,
                        Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.targetUserId = targetUserId;
        this.type = type;
        this.status = status;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.rejectionReason = rejectionReason;
        this.originalTransactionId = originalTransactionId;
        this.createdAt = createdAt;
    }

    
     //Cria uma nova transacao. O status ja deve vir decidido pelo caso de uso
     //apos validar as regras de negocio (saldo, limites, etc).
     
    public static Transaction criar(UUID userId, UUID targetUserId, TransactionType type,
                                    TransactionStatus status, BigDecimal amount, String description,
                                    String category, String rejectionReason, UUID originalTransactionId,
                                    Clock clock) {
        return new Transaction(UUID.randomUUID(), userId, targetUserId, type, status,
                amount, description, category, rejectionReason, originalTransactionId,
                Instant.now(clock));
    }

    /** Preserva a identidade e a data recuperadas da persistencia. */
    public static Transaction reconstituir(UUID id, UUID userId, UUID targetUserId, TransactionType type,
                                           TransactionStatus status, BigDecimal amount, String description,
                                           String category, String rejectionReason, UUID originalTransactionId,
                                           Instant createdAt) {
        return new Transaction(id, userId, targetUserId, type, status, amount, description,
                category, rejectionReason, originalTransactionId, createdAt);
    }

    //apenas çeitura
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getTargetUserId() { return targetUserId; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getRejectionReason() { return rejectionReason; }
    public UUID getOriginalTransactionId() { return originalTransactionId; }
    public Instant getCreatedAt() { return createdAt; }

    // Duas transacoes sao iguais quando possuem o mesmo ID. 
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