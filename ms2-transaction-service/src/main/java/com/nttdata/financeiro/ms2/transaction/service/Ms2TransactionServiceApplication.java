package com.nttdata.financeiro.ms2.transaction.service;

import com.nttdata.financeiro.ms2.transaction.service.config.OpenApiConfiguration;
import com.nttdata.financeiro.ms2.transaction.service.config.RestClientConfiguration;
import com.nttdata.financeiro.ms2.transaction.service.config.TransactionConfiguration;
import com.nttdata.financeiro.ms2.transaction.service.config.WebConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

// Composition root sem component scanning: todas as instancias vem de @Import/@Bean.
@SpringBootConfiguration
@EnableAutoConfiguration
@Import({
        TransactionConfiguration.class,
        WebConfiguration.class,
        RestClientConfiguration.class,
        OpenApiConfiguration.class
})
public class Ms2TransactionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(Ms2TransactionServiceApplication.class, args);
	}

}
