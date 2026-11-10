package com.nttdata.users.application.exception;

public final class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("Usuário não encontrado.");
    }
}
