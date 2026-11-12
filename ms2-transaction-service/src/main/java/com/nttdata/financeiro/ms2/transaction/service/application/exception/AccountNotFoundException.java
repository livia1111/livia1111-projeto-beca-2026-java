package com.nttdata.financeiro.ms2.transaction.service.application.exception;

public final class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException() {
        super("Conta não encontrada no serviço de saldo.");
    }
}