package com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.security;

import com.nttdata.financeiro.ms2.transaction.service.application.exception.InvalidCredentialsException;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BasicAuthAuthenticatorTest {

    private UserServiceClient userServiceClient;
    private BasicAuthAuthenticator authenticator;

    @BeforeEach
    void setUp() {
        userServiceClient = mock(UserServiceClient.class);
        authenticator = new BasicAuthAuthenticator(userServiceClient);
    }

    @Test
    void semHeaderLancaCredenciaisInvalidas() {
        assertThrows(InvalidCredentialsException.class, () -> authenticator.autenticar(null));
        assertThrows(InvalidCredentialsException.class, () -> authenticator.autenticar("Bearer xyz"));
    }

    @Test
    void base64InvalidoLancaCredenciaisInvalidas() {
        assertThrows(InvalidCredentialsException.class,
                () -> authenticator.autenticar("Basic !!!!"));
    }

    @Test
    void credenciaisSemSeparadorLancaCredenciaisInvalidas() {
        String header = "Basic " + Base64.getEncoder()
                .encodeToString("sem-dois-pontos".getBytes(StandardCharsets.UTF_8));
        assertThrows(InvalidCredentialsException.class, () -> authenticator.autenticar(header));
    }

    @Test
    void clienteRetornaNuloLancaCredenciaisInvalidas() {
        when(userServiceClient.autenticar(any(), any())).thenReturn(null);
        String header = basic("ana@example.com", "senha");

        assertThrows(InvalidCredentialsException.class, () -> authenticator.autenticar(header));
    }

    @Test
    void credenciaisValidasDevolveUuidDoMs1() {
        UUID expected = UUID.randomUUID();
        when(userServiceClient.autenticar("ana@example.com", "senha")).thenReturn(expected);

        UUID result = authenticator.autenticar(basic("ana@example.com", "senha"));

        assertEquals(expected, result);
        verify(userServiceClient).autenticar("ana@example.com", "senha");
    }

    private String basic(String email, String password) {
        return "Basic " + Base64.getEncoder()
                .encodeToString((email + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
}
