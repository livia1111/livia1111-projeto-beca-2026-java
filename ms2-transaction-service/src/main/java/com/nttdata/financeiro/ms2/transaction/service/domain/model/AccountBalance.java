package com.nttdata.financeiro.ms2.transaction.service.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

/** Saldo e limites da conta, expressos em reais. */
public record AccountBalance(
        UUID userId,
        BigDecimal balance,
        BigDecimal dailyLimit,
        BigDecimal perTransactionLimit
) {}