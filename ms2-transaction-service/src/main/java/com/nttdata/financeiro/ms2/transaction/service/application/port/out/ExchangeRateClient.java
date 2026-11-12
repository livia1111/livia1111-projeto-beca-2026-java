package com.nttdata.financeiro.ms2.transaction.service.application.port.out;

import com.nttdata.financeiro.ms2.transaction.service.domain.model.ExchangeRateSnapshot;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Porta para consultar câmbio sem acoplar a aplicação à BrasilAPI.
 * Devolve vazio quando a data informada não tem cotação disponível.
 */
public interface ExchangeRateClient {

    Optional<ExchangeRateSnapshot> consultar(LocalDate data);
}
