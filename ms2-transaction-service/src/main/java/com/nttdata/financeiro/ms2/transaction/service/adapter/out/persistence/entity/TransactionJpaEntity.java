package com.nttdata.financeiro.ms2.transaction.service.adapter.out.persistence.entity;

import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionStatus;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class TransactionJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "target_user_id")
    private UUID targetUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String description;

    @Column(length = 50)
    private String category;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "original_transaction_id")
    private UUID originalTransactionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TransactionJpaEntity() {
        // Construtor exigido pelo JPA.
    }

    public TransactionJpaEntity(UUID id, UUID userId, UUID targetUserId,
                                TransactionType type, TransactionStatus status,
                                BigDecimal amount, String description, String category,
                                String rejectionReason, UUID originalTransactionId,
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

    // Getters (JPA usa reflection nos setters, mas para leitura expomos getters)
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
}