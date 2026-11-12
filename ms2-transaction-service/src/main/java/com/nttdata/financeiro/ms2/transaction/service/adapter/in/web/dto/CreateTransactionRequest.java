package com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.dto;

import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionType;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.Category;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

// O valor é sempre em reais; USD e EUR entram apenas na conversão da resposta.
public record CreateTransactionRequest(
        UUID targetUserId,
        @NotNull(message = "O tipo da transação é obrigatório.") TransactionType type,
        @NotNull(message = "O valor é obrigatório.")
        @Positive(message = "O valor deve ser maior que zero.")
        @Digits(integer = 17, fraction = 2, message = "O valor deve ter no máximo 2 casas decimais.")
        BigDecimal amount,
        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres.") String description,
        Category category,
        UUID originalTransactionId
) {}
