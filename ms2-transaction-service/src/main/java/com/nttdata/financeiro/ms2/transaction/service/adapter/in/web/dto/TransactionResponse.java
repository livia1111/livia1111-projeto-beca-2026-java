package com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.dto;

import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionStatus;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionType;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionCurrency;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.Category;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Toda consulta devolve o valor original em real e a conversão em USD e EUR
// com a taxa usada e a data da taxa (snapshot guardado na criação).
public record TransactionResponse(
        UUID id,
        UUID userId,
        UUID targetUserId,
        TransactionType type,
        TransactionStatus status,
        BigDecimal amountBrl,
        ExchangeSection exchange,
        String description,
        Category category,
        String rejectionReason,
        UUID originalTransactionId,
        Instant createdAt
) {
    // Nulo apenas em rejeição causada pela ausência de cotação de câmbio.
    public record ExchangeSection(LocalDate rateDate, List<Conversion> conversions) {}

    public record Conversion(TransactionCurrency currency, BigDecimal amount, BigDecimal rate) {}
}
