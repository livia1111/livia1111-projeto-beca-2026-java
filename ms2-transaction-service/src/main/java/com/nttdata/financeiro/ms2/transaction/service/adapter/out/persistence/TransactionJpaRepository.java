package com.nttdata.financeiro.ms2.transaction.service.adapter.out.persistence;

import com.nttdata.financeiro.ms2.transaction.service.adapter.out.persistence.entity.TransactionJpaEntity;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionStatus;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID> {

    /**
     * Lista transações com filtros opcionais.
     * Parâmetros null são ignorados pelo COALESCE.
     */
    @Query("""
            SELECT t FROM TransactionJpaEntity t
            WHERE t.userId = :userId
              AND (:inicio IS NULL OR t.createdAt >= :inicio)
              AND (:fim IS NULL OR t.createdAt <= :fim)
              AND (:tipo IS NULL OR t.type = :tipo)
            ORDER BY t.createdAt DESC
            """)
    List<TransactionJpaEntity> listar(
            @Param("userId") UUID userId,
            @Param("inicio") Instant inicio,
            @Param("fim") Instant fim,
            @Param("tipo") TransactionType tipo
    );

    /**
     * Calcula o saldo: soma depósitos e subtrai saídas aprovadas.
     */
    @Query("""
            SELECT COALESCE(SUM(
                CASE
                    WHEN t.type = 'DEPOSITO' THEN t.amount
                    ELSE -t.amount
                END
            ), 0)
            FROM TransactionJpaEntity t
            WHERE t.userId = :userId
              AND t.status = 'APROVADA'
            """)
    BigDecimal calcularSaldo(@Param("userId") UUID userId);
}