package com.nttdata.financeiro.ms2.transaction.service.adapter.out.client;

import com.nttdata.financeiro.ms2.transaction.service.application.port.out.ExchangeRateClient;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.ExchangeRateSnapshot;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Optional;

/**
 * Consulta /cambio/v1/cotacao/{moeda}/{data} da BrasilAPI para USD e EUR.
 * Para fim de semana/feriado a própria API devolve o último dia útil.
 * Qualquer falha vira Optional.empty() — a decisão de rejeitar é do caso de uso.
 */
public final class BrasilApiExchangeRateAdapter implements ExchangeRateClient {

    private final RestClient restClient;

    public BrasilApiExchangeRateAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Optional<ExchangeRateSnapshot> consultar(LocalDate data) {
        Optional<Quote> usd = cotacao("USD", data);
        if (usd.isEmpty()) {
            return Optional.empty();
        }
        Optional<Quote> eur = cotacao("EUR", data);
        if (eur.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ExchangeRateSnapshot(
                usd.get().date(), usd.get().sellRate(), eur.get().sellRate()));
    }

    private Optional<Quote> cotacao(String moeda, LocalDate data) {
        try {
            BrasilApiResponse response = restClient.get()
                    .uri("/cambio/v1/cotacao/{moeda}/{data}", moeda, data)
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
                    .map(quote -> new Quote(quote.cotacao_venda(), returnedDate));
        } catch (RestClientException | DateTimeParseException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private record Quote(BigDecimal sellRate, LocalDate date) {}

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
