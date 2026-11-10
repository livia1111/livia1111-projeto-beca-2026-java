package com.nttdata.users.adapter.out.persistence;

import com.nttdata.users.application.exception.EmailAlreadyExistsException;
import org.hibernate.exception.ConstraintViolationException;

/** Traduz uma falha técnica conhecida sem depender de mensagens SQL do fornecedor. */
public final class UserPersistenceExceptionTranslator {

    public RuntimeException translate(RuntimeException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation
                    && "uk_users_email".equals(violation.getConstraintName())) {
                return new EmailAlreadyExistsException();
            }
            if (cause.getCause() == cause) {
                break;
            }
            cause = cause.getCause();
        }
        return exception;
    }
}
