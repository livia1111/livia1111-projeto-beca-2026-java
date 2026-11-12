package com.nttdata.financeiro.ms2.transaction.service.application.exception;

public final class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException() {
        super("Transação não encontrada.");
    }
}