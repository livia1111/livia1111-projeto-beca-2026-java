package com.nttdata.financeiro.ms2.transaction.service.domain.service;

import com.nttdata.financeiro.ms2.transaction.service.domain.model.AccountBalance;

import java.math.BigDecimal;
import java.util.Optional;

/** Regras puras de aprovação para transações de saída. */
public final class TransactionApprovalPolicy {

    public Optional<String> avaliar(BigDecimal amount, AccountBalance account, BigDecimal spentToday) {
        if (amount.compareTo(account.perTransactionLimit()) > 0) {
            return Optional.of("Valor excede o limite por transação.");
        }
        if (spentToday.add(amount).compareTo(account.dailyLimit()) > 0) {
            return Optional.of("Valor excede o limite diário.");
        }
        if (amount.compareTo(account.balance()) > 0) {
            return Optional.of("Saldo insuficiente.");
        }
        return Optional.empty();
    }
}