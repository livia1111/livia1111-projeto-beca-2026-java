package com.nttdata.financeiro.ms2.transaction.service.adapter.out.client;

import com.nttdata.financeiro.ms2.transaction.service.application.exception.AccountServiceUnavailableException;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.AccountServiceClient;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.AccountBalance;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Adaptador temporário. Substituir quando a API Mock e seu contrato estiverem disponíveis.
 * Falha de forma fechada: não aprova nem grava transações sem consultar saldo e limite.
 */
public final class UnavailableAccountServiceClientAdapter implements AccountServiceClient {

    @Override
    public AccountBalance consultar(UUID userId) {
        throw new AccountServiceUnavailableException();
    }

    @Override
    public void ajustarSaldo(UUID userId, BigDecimal delta) {
        throw new AccountServiceUnavailableException();
    }
}