package com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.mapper;

import com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.dto.CreateTransactionRequest;
import com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.dto.TransactionResponse;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.CreateTransactionCommand;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.ExchangeRateSnapshot;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.Transaction;
import java.util.List;
import java.util.UUID;

public final class TransactionWebMapper {

    public CreateTransactionCommand toCommand(CreateTransactionRequest request, UUID authenticatedUserId) {
        return new CreateTransactionCommand(
                authenticatedUserId,
                request.targetUserId(),
                request.type(),
                request.amount(),
                request.description(),
                request.category(),
                request.originalTransactionId()
        );
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getTargetUserId(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getAmount(),
                toExchangeSection(transaction),
                transaction.getDescription(),
                transaction.getCategory(),
                transaction.getRejectionReason(),
                transaction.getOriginalTransactionId(),
                transaction.getCreatedAt()
        );
    }

    private TransactionResponse.ExchangeSection toExchangeSection(Transaction transaction) {
        ExchangeRateSnapshot snapshot = transaction.getExchangeRate();
        if (snapshot == null) {
            return null;
        }
        List<TransactionResponse.Conversion> conversions = transaction.getConversions().stream()
                .map(conversion -> new TransactionResponse.Conversion(
                        conversion.currency(), conversion.amount(), conversion.rate()))
                .toList();
        return new TransactionResponse.ExchangeSection(snapshot.rateDate(), conversions);
    }
}
