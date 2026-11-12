package com.nttdata.financeiro.ms2.transaction.service.application.exception;

public final class AccountServiceUnavailableException extends RuntimeException {
    public AccountServiceUnavailableException() {
        super("Serviço de contas indisponível. Tente novamente mais tarde.");
    }
}