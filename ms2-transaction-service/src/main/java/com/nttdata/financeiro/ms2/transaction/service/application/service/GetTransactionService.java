package com.nttdata.financeiro.ms2.transaction.service.application.service;

import com.nttdata.financeiro.ms2.transaction.service.application.exception.AccessDeniedToTransactionException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.TransactionNotFoundException;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.GetTransactionUseCase;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.TransactionRepository;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.Transaction;

import java.util.UUID;

public class GetTransactionService implements GetTransactionUseCase {

    private final TransactionRepository repository;

    public GetTransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Transaction buscar(UUID id, UUID requesterId) {
        Transaction transaction = repository.buscarPorId(id)
                .orElseThrow(TransactionNotFoundException::new);

        if (!transaction.getUserId().equals(requesterId)) {
            throw new AccessDeniedToTransactionException();
        }

        return transaction;
    }
}