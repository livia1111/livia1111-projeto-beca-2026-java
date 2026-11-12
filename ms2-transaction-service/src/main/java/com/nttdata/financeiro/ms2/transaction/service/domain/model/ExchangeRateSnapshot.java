package com.nttdata.financeiro.ms2.transaction.service.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Snapshot imutavel das taxas de cambio guardadas na criacao da transacao.
 * A conversao sempre sai de BRL para USD e EUR usando a taxa de venda.
 */
public record ExchangeRateSnapshot(LocalDate rateDate, BigDecimal usdRate, BigDecimal eurRate) {

    public ExchangeRateSnapshot {
        if (rateDate == null) {
            throw new IllegalArgumentException("A data da taxa é obrigatória.");
        }
        if (usdRate == null || usdRate.signum() <= 0) {
            throw new IllegalArgumentException("A taxa do USD deve ser maior que zero.");
        }
        if (eurRate == null || eurRate.signum() <= 0) {
            throw new IllegalArgumentException("A taxa do EUR deve ser maior que zero.");
        }
    }
}
