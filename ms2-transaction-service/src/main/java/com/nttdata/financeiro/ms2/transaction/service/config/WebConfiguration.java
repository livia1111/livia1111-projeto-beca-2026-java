package com.nttdata.financeiro.ms2.transaction.service.config;

import com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.controller.TransactionController;
import com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.error.GlobalExceptionHandler;
import com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.mapper.TransactionWebMapper;
import com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.security.BasicAuthAuthenticator;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.CreateTransactionUseCase;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.GetAccountBalanceUseCase;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.GetTransactionUseCase;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.ListTransactionsUseCase;
import com.nttdata.financeiro.ms2.transaction.service.application.port.out.UserServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class WebConfiguration {

    @Bean
    public TransactionWebMapper transactionWebMapper() {
        return new TransactionWebMapper();
    }

    @Bean
    public BasicAuthAuthenticator basicAuthAuthenticator(UserServiceClient userServiceClient) {
        return new BasicAuthAuthenticator(userServiceClient);
    }

    @Bean
    public TransactionController transactionController(CreateTransactionUseCase createUseCase,
                                                       GetTransactionUseCase getUseCase,
                                                       ListTransactionsUseCase listUseCase,
                                                       GetAccountBalanceUseCase balanceUseCase,
                                                       TransactionWebMapper mapper,
                                                       BasicAuthAuthenticator authenticator) {
        return new TransactionController(createUseCase, getUseCase, listUseCase,
                balanceUseCase, mapper, authenticator);
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
