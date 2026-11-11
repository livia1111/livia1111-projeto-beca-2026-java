package com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.dto;

import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionCurrency;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransactionRequest(
        UUID targetUserId,

        @NotNull(message = "O tipo da transação é obrigatório.")
        TransactionType type,

        @NotNull(message = "O valor é obrigatório.")
        @Positive(message = "O valor deve ser maior que zero.")
        BigDecimal amount,

        @NotNull(message = "A moeda deve ser USD ou EUR.")
        TransactionCurrency currency,

        String description,
        String category,
        UUID originalTransactionId
) {}