package com.nttdata.financeiro.ms2.transaction.service.adapter.out.client;

import com.nttdata.financeiro.ms2.transaction.service.application.port.out.ExchangeRateClient;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionCurrency;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Optional;

public final class BrasilApiExchangeRateAdapter implements ExchangeRateClient {

    private final RestClient restClient;

    public BrasilApiExchangeRateAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Optional<ExchangeRate> consultar(TransactionCurrency currency, LocalDate requestedDate) {
        try {
            BrasilApiResponse response = restClient.get()
                    .uri("/cambio/v1/cotacao/{moeda}/{data}", currency.name(), requestedDate)
                    .retrieve()
                    .body(BrasilApiResponse.class);

            if (response == null || response.data() == null || response.cotacoes() == null) {
                return Optional.empty();
            }

            LocalDate returnedDate = LocalDate.parse(response.data());

            return response.cotacoes().stream()
                    .filter(quote -> quote.cotacao_compra() != null
                            && quote.cotacao_compra().signum() > 0
                            && quote.cotacao_venda() != null
                            && quote.cotacao_venda().signum() > 0)
                    .max(Comparator.comparing(
                            BrasilApiQuote::data_hora_cotacao,
                            Comparator.nullsFirst(Comparator.naturalOrder())
                    ))
                    .map(quote -> new ExchangeRate(
                            quote.cotacao_compra(),
                            quote.cotacao_venda(),
                            returnedDate
                    ));
        } catch (RestClientException | DateTimeParseException ex) {
            return Optional.empty();
        }
    }

    private record BrasilApiResponse(
            String moeda,
            String data,
            java.util.List<BrasilApiQuote> cotacoes
    ) {}

    private record BrasilApiQuote(
            BigDecimal cotacao_compra,
            BigDecimal cotacao_venda,
            String data_hora_cotacao
    ) {}
}