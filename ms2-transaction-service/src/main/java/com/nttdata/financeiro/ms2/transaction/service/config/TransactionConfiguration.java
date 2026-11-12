package com.nttdata.financeiro.ms2.transaction.service.config;

import com.nttdata.financeiro.ms2.transaction.service.adapter.out.client.UserServiceClientAdapter;
import com.nttdata.financeiro.ms2.transaction.service.adapter.out.client.MockApiAccountServiceAdapter;
import com.nttdata.financeiro.ms2.transaction.service.adapter.out.client.BrasilApiExchangeRateAdapter;
import com.nttdata.financeiro.ms2.transaction.service.adapter.out.persistence.TransactionJpaRepository;
import com.nttdata.financeiro.ms2.transaction.service.adapter.out.persistence.TransactionPersistenceAdapter;
import com.nttdata.financeiro.ms2.transaction.service.adapter.out.persistence.TransactionPersistenceExceptionTranslator;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.CreateTransactionUseCase;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.GetAccountBalanceUseCase;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.GetTransactionUseCase;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.ListTransactionsUseCase;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.TransactionRepository;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.AccountServiceClient;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.ExchangeRateClient;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.UserServiceClient;
import com.nttdata.financeiro.ms2.transaction.service.application.service.CreateTransactionService;
import com.nttdata.financeiro.ms2.transaction.service.application.service.GetAccountBalanceService;
import com.nttdata.financeiro.ms2.transaction.service.application.service.GetTransactionService;
import com.nttdata.financeiro.ms2.transaction.service.application.service.ListTransactionsService;
import com.nttdata.financeiro.ms2.transaction.service.domain.service.TransactionApprovalPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestClient;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(basePackageClasses = TransactionJpaRepository.class)
public class TransactionConfiguration {

    // --- Utilitários ---
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    // --- Adaptadores de Saída (Persistência) ---
    @Bean
    public TransactionPersistenceExceptionTranslator transactionPersistenceExceptionTranslator() {
        return new TransactionPersistenceExceptionTranslator();
    }

    @Bean
    public TransactionRepository transactionRepository(TransactionJpaRepository jpaRepository,
                                                       TransactionPersistenceExceptionTranslator translator) {
        return new TransactionPersistenceAdapter(jpaRepository, translator);
    }

    // --- Adaptadores de Saída (Comunicação MS1) ---
    @Bean
    public UserServiceClient userServiceClient(@Qualifier("ms1RestClient") RestClient ms1RestClient) {
        return new UserServiceClientAdapter(ms1RestClient);
    }

    @Bean
    public AccountServiceClient accountServiceClient(@Qualifier("mockApiRestClient") RestClient mockApiRestClient) {
        return new MockApiAccountServiceAdapter(mockApiRestClient);
    }

    @Bean
    public ExchangeRateClient exchangeRateClient(
            @Qualifier("brasilApiRestClient") RestClient brasilApiRestClient
    ) {
        return new BrasilApiExchangeRateAdapter(brasilApiRestClient);
    }

    // --- Casos de Uso (Services) ---
    @Bean
    public CreateTransactionUseCase createTransactionUseCase(TransactionRepository repository,
                                                             UserServiceClient userServiceClient,
                                                             AccountServiceClient accountServiceClient,
                                                             ExchangeRateClient exchangeRateClient,
                                                             TransactionApprovalPolicy transactionApprovalPolicy,
                                                             Clock clock) {
        return new CreateTransactionService(
                repository,
                userServiceClient,
                accountServiceClient,
                exchangeRateClient,
                transactionApprovalPolicy,
                clock
        );
    }

    @Bean
    public GetAccountBalanceUseCase getAccountBalanceUseCase(AccountServiceClient accountServiceClient) {
        return new GetAccountBalanceService(accountServiceClient);
    }

    @Bean
    public TransactionApprovalPolicy transactionApprovalPolicy() {
        return new TransactionApprovalPolicy();
    }

    @Bean
    public GetTransactionUseCase getTransactionUseCase(TransactionRepository repository) {
        return new GetTransactionService(repository);
    }

    @Bean
    public ListTransactionsUseCase listTransactionsUseCase(TransactionRepository repository) {
        return new ListTransactionsService(repository);
    }
}