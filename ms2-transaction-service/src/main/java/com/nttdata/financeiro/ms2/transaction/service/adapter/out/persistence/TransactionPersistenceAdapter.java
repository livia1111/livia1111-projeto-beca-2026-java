package com.nttdata.financeiro.ms2.transaction.service.adapter.out.persistence;

import com.nttdata.financeiro.ms2.transaction.service.adapter.out.persistence.entity.TransactionJpaEntity;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.TransactionRepository;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.Transaction;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TransactionPersistenceAdapter implements TransactionRepository {

    private final TransactionJpaRepository repository;
    private final TransactionPersistenceExceptionTranslator translator;

    public TransactionPersistenceAdapter(TransactionJpaRepository repository,
                                         TransactionPersistenceExceptionTranslator translator) {
        this.repository = repository;
        this.translator = translator;
    }

    @Override
    public Transaction salvar(Transaction transaction) {
        try {
            TransactionJpaEntity entity = paraEntidade(transaction);
            TransactionJpaEntity saved = repository.saveAndFlush(entity);
            return paraDominio(saved);
        } catch (RuntimeException ex) {
            throw translator.translate(ex);
        }
    }

    @Override
    public Optional<Transaction> buscarPorId(UUID id) {
        return repository.findById(id).map(this::paraDominio);
    }

    @Override
    public List<Transaction> listar(UUID userId, Instant inicio, Instant fim, TransactionType tipo) {
        return repository.listar(userId, inicio, fim, tipo).stream()
                .map(this::paraDominio)
                .toList();
    }

    @Override
    public BigDecimal calcularSaldo(UUID userId) {
        return repository.calcularSaldo(userId);
    }

    // ==========================================
    // Mapeamento Entidade ↔ Domínio
    // ==========================================

    private TransactionJpaEntity paraEntidade(Transaction t) {
        return new TransactionJpaEntity(
                t.getId(), t.getUserId(), t.getTargetUserId(),
                t.getType(), t.getStatus(), t.getAmount(),
                t.getDescription(), t.getCategory(), t.getRejectionReason(),
                t.getOriginalTransactionId(), t.getCreatedAt()
        );
    }

    private Transaction paraDominio(TransactionJpaEntity e) {
        return Transaction.reconstituir(
                e.getId(), e.getUserId(), e.getTargetUserId(),
                e.getType(), e.getStatus(), e.getAmount(),
                e.getDescription(), e.getCategory(), e.getRejectionReason(),
                e.getOriginalTransactionId(), e.getCreatedAt()
        );
    }
}