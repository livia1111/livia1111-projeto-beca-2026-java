package com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.dto;

import com.nttdata.financeiro.ms2.transaction.service.domain.model.AccountBalance;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Saldo atual e limites em reais retornados pelo serviço de contas")
public record AccountBalanceResponse(
        UUID userId,
        @Schema(example = "1500.00") BigDecimal balance,
        @Schema(example = "500.00") BigDecimal dailyLimit,
        @Schema(example = "200.00") BigDecimal perTransactionLimit
) {
    public static AccountBalanceResponse from(AccountBalance account) {
        return new AccountBalanceResponse(account.userId(), account.balance(),
                account.dailyLimit(), account.perTransactionLimit());
    }
}