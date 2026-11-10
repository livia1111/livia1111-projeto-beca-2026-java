package com.nttdata.users.application.exception;

public final class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() {
        super("Já existe um usuário cadastrado com este e-mail.");
    }
}
