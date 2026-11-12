package com.nttdata.financeiro.ms2.transaction.service.application.exception;

public final class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("E-mail ou senha incorretos.");
    }
}