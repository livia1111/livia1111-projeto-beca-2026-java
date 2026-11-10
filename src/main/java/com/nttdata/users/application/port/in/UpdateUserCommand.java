package com.nttdata.users.application.port.in;

public record UpdateUserCommand(String nome, String email, String senha) {
    @Override
    public String toString() { return "UpdateUserCommand[conteudo omitido]"; }
}
