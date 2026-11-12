package com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.dto;

import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionStatus;

import java.util.UUID;

public record CreatedTransactionResponse(
        UUID id,
        TransactionStatus status
) {}