package com.nttdata.financeiro.ms2.transaction.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
public class RestClientConfiguration {

    @Bean
    public RestClient ms1RestClient(@Value("${ms1.api.url}") String ms1ApiUrl) {
        return RestClient.builder()
                .baseUrl(ms1ApiUrl)
                .build();
    }

    @Bean
    public RestClient brasilApiRestClient(
            @Value("${BRASILAPI_API_URL:https://brasilapi.com.br/api}") String brasilApiUrl
    ) {
        return RestClient.builder()
                .baseUrl(brasilApiUrl)
                .build();
    }

    @Bean
    public RestClient mockApiRestClient(@Value("${mockapi.api.url}") String mockApiUrl) {
        return RestClient.builder().baseUrl(mockApiUrl).build();
    }
}