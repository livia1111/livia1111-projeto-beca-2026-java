package com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.security;

import com.nttdata.financeiro.ms2.transaction.service.application.exception.InvalidCredentialsException;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.UserServiceClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * Decodifica o cabeçalho HTTP Basic (concern do adapter de entrada) e
 * valida as credenciais no MS1 pela porta UserServiceClient.
 */
public final class BasicAuthAuthenticator {

    private final UserServiceClient userServiceClient;

    public BasicAuthAuthenticator(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    public UUID autenticar(String authorization) {
        if (authorization == null
                || !authorization.regionMatches(true, 0, "Basic ", 0, 6)) {
            throw new InvalidCredentialsException();
        }

        try {
            String credentials = new String(
                    Base64.getDecoder().decode(authorization.substring(6).trim()),
                    StandardCharsets.UTF_8
            );

            int separator = credentials.indexOf(':');
            if (separator <= 0) {
                throw new InvalidCredentialsException();
            }

            UUID userId = userServiceClient.autenticar(
                    credentials.substring(0, separator),
                    credentials.substring(separator + 1)
            );

            if (userId == null) {
                throw new InvalidCredentialsException();
            }

            return userId;
        } catch (IllegalArgumentException ex) {
            throw new InvalidCredentialsException();
        }
    }
}
