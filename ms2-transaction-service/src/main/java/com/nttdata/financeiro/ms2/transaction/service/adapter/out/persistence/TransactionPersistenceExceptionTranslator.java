package com.nttdata.financeiro.ms2.transaction.service.adapter.out.persistence;

/**
 * Traduz falhas técnicas conhecidas sem depender de mensagens SQL do fornecedor.
 * Por enquanto vazio (não há constraint customizada), mas mantemos o padrão
 * igual ao MS1 (UserPersistenceExceptionTranslator) para simetria.
 */
public final class TransactionPersistenceExceptionTranslator {

    public RuntimeException translate(RuntimeException exception) {
        // Se no futuro houver constraint como uk_transactions_xxx, tratar aqui.
        return exception;
    }
}