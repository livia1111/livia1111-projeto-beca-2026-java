package com.nttdata.financeiro.ms2.transaction.service.application.port.out;

import com.nttdata.financeiro.ms2.transaction.service.domain.model.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Porta de saída para persistência de transações. */
public interface TransactionRepository {

    Transaction salvar(Transaction transaction);

    Optional<Transaction> buscarPorId(UUID id);

    List<Transaction> listarPorUsuario(UUID userId);

   //Calcula o saldo atual do usuário considerando apenas transações aprovadas
   //soma DEPOSITO e subtrai RETIRADA, TRANSFERENCIA (enviadas) e COMPRA 
   BigDecimal calcularSaldo(UUID userId);
}