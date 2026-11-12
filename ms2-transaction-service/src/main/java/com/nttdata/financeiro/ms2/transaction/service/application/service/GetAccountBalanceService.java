package com.nttdata.financeiro.ms2.transaction.service.application.service;

import com.nttdata.financeiro.ms2.transaction.service.application.port.in.GetAccountBalanceUseCase;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.AccountServiceClient;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.AccountBalance;

import java.util.UUID;

public class GetAccountBalanceService implements GetAccountBalanceUseCase {

    private final AccountServiceClient accountService;

    public GetAccountBalanceService(AccountServiceClient accountService) {
        this.accountService = accountService;
    }

    @Override
    public AccountBalance consultar(UUID userId) {
        return accountService.consultar(userId);
    }
}