package com.nttdata.financeiro.ms2.transaction.service.application.exception;

public final class TargetUserNotFoundException extends RuntimeException {
    public TargetUserNotFoundException() {
        super("Usuário destinatário não encontrado.");
    }
}