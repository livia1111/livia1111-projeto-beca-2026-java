package com.nttdata.financeiro.ms2.transaction.service.application.exception;

public final class AccessDeniedToTransactionException extends RuntimeException {
    public AccessDeniedToTransactionException() {
        super("Você não tem permissão para acessar esta transação.");
    }
}