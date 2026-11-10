package com.nttdata.users.application.exception;

public final class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("E-mail ou senha incorretos.");
    }
}
