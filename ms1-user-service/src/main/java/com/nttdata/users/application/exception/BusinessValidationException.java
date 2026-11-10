package com.nttdata.users.application.exception;

public final class BusinessValidationException extends RuntimeException {
    /** Recebe apenas mensagens controladas pela aplicação, nunca erros técnicos. */
    public BusinessValidationException(String message) {
        super(message);
    }
}
