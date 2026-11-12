package com.nttdata.financeiro.ms2.transaction.service.adapter.out.client;

import com.nttdata.financeiro.ms2.transaction.service.application.exception.AccountNotFoundException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.AccountServiceUnavailableException;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.AccountServiceClient;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.AccountBalance;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Adapter for the Mock API account resource. All monetary values are BRL. */
public final class MockApiAccountServiceAdapter implements AccountServiceClient {

    private final RestClient restClient;

    public MockApiAccountServiceAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public AccountBalance consultar(UUID userId) {
        MockAccount account = buscarConta(userId);
        return new AccountBalance(account.userId(), account.balance(),
                account.dailyLimit(), account.perTransactionLimit());
    }

    @Override
    public void ajustarSaldo(UUID userId, BigDecimal delta) {
        if (delta == null) {
            throw new IllegalArgumentException("O ajuste do saldo é obrigatório.");
        }
        MockAccount account = buscarConta(userId);
        BigDecimal updatedBalance = account.balance().add(delta);
        if (updatedBalance.signum() < 0) {
            throw new AccountServiceUnavailableException();
        }
        MockAccount updatedAccount = new MockAccount(account.id(), account.userId(), updatedBalance,
                account.dailyLimit(), account.perTransactionLimit());
        try {
            restClient.put()
                    .uri("/balance/{id}", account.id())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(updatedAccount)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new AccountServiceUnavailableException();
        }
    }

    private MockAccount buscarConta(UUID userId) {
        try {
            List<MockAccount> accounts = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/balance")
                            .queryParam("userId", userId).build())
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<>() {});
            if (accounts == null || accounts.isEmpty()) throw new AccountNotFoundException();
            MockAccount account = accounts.stream()
                    .filter(item -> item != null && userId.equals(item.userId()))
                    .findFirst()
                    .orElseThrow(AccountNotFoundException::new);
            if (account.balance() == null || account.dailyLimit() == null
                    || account.perTransactionLimit() == null
                    || account.balance().signum() < 0 || account.dailyLimit().signum() < 0
                    || account.perTransactionLimit().signum() < 0) {
                throw new AccountServiceUnavailableException();
            }
            return account;
        } catch (AccountNotFoundException | AccountServiceUnavailableException ex) {
            throw ex;
        } catch (RestClientException | IllegalArgumentException ex) {
            throw new AccountServiceUnavailableException();
        }
    }

    public record MockAccount(String id, UUID userId, BigDecimal balance,
                              BigDecimal dailyLimit, BigDecimal perTransactionLimit) {}
}