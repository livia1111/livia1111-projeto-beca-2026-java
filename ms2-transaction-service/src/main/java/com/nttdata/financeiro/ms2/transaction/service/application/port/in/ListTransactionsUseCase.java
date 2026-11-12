package com.nttdata.financeiro.ms2.transaction.service.application.port.in;

import com.nttdata.financeiro.ms2.transaction.service.domain.model.Transaction;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Caso de uso: listar transações de um usuário, com filtros opcionais. */
public interface ListTransactionsUseCase {

    // Dados de entrada do caso de uso de criação de transação. userId vem do contexto de autenticação. 
    List<Transaction> listar(UUID userId, Instant inicio, Instant fim, TransactionType tipo);
}