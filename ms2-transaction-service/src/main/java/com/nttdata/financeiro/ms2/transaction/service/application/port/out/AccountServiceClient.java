package com.nttdata.financeiro.ms2.transaction.service.application.port.out;

import com.nttdata.financeiro.ms2.transaction.service.domain.model.AccountBalance;
import java.math.BigDecimal;
import java.util.UUID;

/** Porta para consultar saldo e limite na API Mock. */
public interface AccountServiceClient {

    /** Retorna saldo e limite da conta; não consulta câmbio. */
    AccountBalance consultar(UUID userId);

    /** Aplica ao saldo externo um delta em reais: positivo credita, negativo debita. */
    void ajustarSaldo(UUID userId, BigDecimal delta);
}