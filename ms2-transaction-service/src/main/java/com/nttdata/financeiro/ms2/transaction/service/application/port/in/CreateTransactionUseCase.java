package com.nttdata.financeiro.ms2.transaction.service.application.port.in;

import com.nttdata.financeiro.ms2.transaction.service.domain.model.Transaction;

/** Caso de uso: registrar uma nova transação (depósito, retirada, transferência ou compra). */
public interface CreateTransactionUseCase {

    Transaction criar(CreateTransactionCommand command);
}