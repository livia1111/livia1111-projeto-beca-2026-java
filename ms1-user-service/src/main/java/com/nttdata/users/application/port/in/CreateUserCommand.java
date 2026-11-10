package com.nttdata.users.application.port.in;

import com.nttdata.users.domain.model.Role;

/**
 * Dados de entrada do caso de uso de cadastro de usuario.
 * Diferente de CreateUserRequest, que pertence ao adaptador HTTP, este objeto
 * nao depende de Spring, JSON ou Bean Validation. Assim, o mesmo caso de uso
 * pode receber dados do controller ou da importacao CSV.
 * O nome Command expressa a intencao de executar uma operacao; nao exige CQRS.
 */
public record CreateUserCommand(String nome, String email, String senha, Role role) {

    @Override
    public String toString() {
        return "CreateUserCommand[conteudo omitido]";
    }
}
