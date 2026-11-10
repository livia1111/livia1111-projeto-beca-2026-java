package com.nttdata.financeiro.ms2.transaction.service.application.port.in;

import com.nttdata.financeiro.ms2.transaction.service.domain.model.Transaction;

import java.util.UUID;

/** Caso de uso: consultar uma transação pelo identificador. */
public interface GetTransactionUseCase {

    //Caso de uso: consultar uma transação pelo identificador, validando que o solicitante é o dono.
    Transaction buscar(UUID id, UUID requesterId);
}