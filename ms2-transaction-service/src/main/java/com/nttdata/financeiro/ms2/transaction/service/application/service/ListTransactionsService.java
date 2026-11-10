package com.nttdata.financeiro.ms2.transaction.service.application.service;

import com.nttdata.financeiro.ms2.transaction.service.application.port.in.ListTransactionsUseCase;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.TransactionRepository;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.Transaction;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ListTransactionsService implements ListTransactionsUseCase {

    private final TransactionRepository repository;

    public ListTransactionsService(TransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Transaction> listar(UUID userId, Instant inicio, Instant fim, TransactionType tipo) {
        return repository.listar(userId, inicio, fim, tipo);
    }
}