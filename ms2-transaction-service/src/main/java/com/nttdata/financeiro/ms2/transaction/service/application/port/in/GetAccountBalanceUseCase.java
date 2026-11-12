package com.nttdata.financeiro.ms2.transaction.service.application.port.in;

import com.nttdata.financeiro.ms2.transaction.service.domain.model.AccountBalance;

import java.util.UUID;

/** Consulta saldo e limites atuais da conta autenticada no serviço externo. */
public interface GetAccountBalanceUseCase {
    AccountBalance consultar(UUID userId);
}