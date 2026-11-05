package com.nttdata.ms1.domain.exception;

public class DuplicateEmailException extends DomainException {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
