package com.nttdata.financeiro.ms2.transaction.service.application.port.out;

import java.util.UUID;

/** Porta de saída para comunicação com o MS1 (User Service). */
public interface UserServiceClient {

    /**
     * Valida credenciais (email + senha) contra o MS1.
     * Retorna o UUID do usuário autenticado.
     *
     * @throws com.nttdata.financeiro.ms2.transaction.service.application.exception.UserServiceUnavailableException
     *         se o MS1 estiver fora do ar.
     * @throws com.nttdata.financeiro.ms2.transaction.service.application.exception.InvalidCredentialsException
     *         se as credenciais forem inválidas.
     */
    UUID autenticar(String email, String password);

    /**
     * Verifica se um usuário existe no MS1 (usado para validar destinatário de transferência).
     *
     * @throws com.nttdata.financeiro.ms2.transaction.service.application.exception.UserServiceUnavailableException
     *         se o MS1 estiver fora do ar.
     */
    boolean usuarioExiste(UUID userId);
}