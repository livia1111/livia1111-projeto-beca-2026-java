package com.nttdata.financeiro.ms2.transaction.service.adapter.out.client;

import com.nttdata.financeiro.ms2.transaction.service.application.exception.InvalidCredentialsException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.UserServiceUnavailableException;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.UserServiceClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.UUID;

public class UserServiceClientAdapter implements UserServiceClient {

    private final RestClient restClient;

    public UserServiceClientAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public UUID autenticar(String email, String password) {
        try {
            LoginResponse response = restClient.post()
                    .uri("/auth/login")
                    .headers(headers -> headers.setBasicAuth(email, password))
                    .retrieve()
                    .body(LoginResponse.class);
            return response != null ? response.id() : null;
        } catch (HttpClientErrorException.Unauthorized ex) {
            // Usando a nossa exceção pura de domínio em vez da do Spring Security
            throw new InvalidCredentialsException();
        } catch (ResourceAccessException ex) {
            throw new UserServiceUnavailableException();
        }
    }

    @Override
    public boolean usuarioExiste(UUID userId) {
        try {
            restClient.get()
                    .uri("/users/{id}/exists", userId)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound ex) {
            return false;
        } catch (ResourceAccessException ex) {
            throw new UserServiceUnavailableException();
        }
    }

    // Record interno apenas para mapear a resposta do MS1
    record LoginResponse(UUID id) {}
}