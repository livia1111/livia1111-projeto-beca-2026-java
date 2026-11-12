package com.nttdata.financeiro.ms2.transaction.service.application.port.in;

import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionType;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.Category;

import java.math.BigDecimal;
import java.util.UUID;


//Dados de entrada do caso de uso de criação de transação.
//userId vem do contexto de autenticação; amount é sempre em BRL.

public record CreateTransactionCommand(
        UUID userId,
        UUID targetUserId,
        TransactionType type,
        BigDecimal amount,
        String description,
        Category category,
        UUID originalTransactionId
) {
    @Override
    public String toString() {
        return "CreateTransactionCommand[conteudo omitido]";
    }
}
