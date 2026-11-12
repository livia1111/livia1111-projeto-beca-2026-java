package com.nttdata.financeiro.ms2.transaction.service.application.exception;

public final class UserServiceUnavailableException extends RuntimeException {
    public UserServiceUnavailableException() {
        super("Serviço de usuários indisponível. Tente novamente mais tarde.");
    }
}