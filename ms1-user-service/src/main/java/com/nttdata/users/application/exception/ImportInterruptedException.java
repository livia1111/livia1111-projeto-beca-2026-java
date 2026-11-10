package com.nttdata.users.application.exception;

import com.nttdata.users.application.port.in.ImportUsersResult;

public class ImportInterruptedException extends RuntimeException {
    private final ImportUsersResult parcial;
    private final int codigo;

    public ImportInterruptedException(String mensagem, int codigo, ImportUsersResult parcial) {
        super(mensagem);
        this.codigo = codigo;
        this.parcial = parcial;
    }

    public ImportUsersResult getParcial() { return parcial; }
    public int getCodigo() { return codigo; }
}
