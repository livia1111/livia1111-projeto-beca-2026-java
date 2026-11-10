package com.nttdata.financeiro.ms2.transaction.service.application.port.out;

import com.nttdata.financeiro.ms2.transaction.service.domain.model.Transaction;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {

    Transaction salvar(Transaction transaction);

    Optional<Transaction> buscarPorId(UUID id);

    List<Transaction> listar(UUID userId, Instant inicio, Instant fim, TransactionType tipo);

    BigDecimal calcularSaldo(UUID userId);
}