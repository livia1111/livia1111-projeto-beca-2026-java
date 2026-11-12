package com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.controller;

import com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.dto.AccountBalanceResponse;
import com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.dto.CreateTransactionRequest;
import com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.dto.CreatedTransactionResponse;
import com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.dto.TransactionResponse;
import com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.mapper.TransactionWebMapper;
import com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.security.BasicAuthAuthenticator;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.CreateTransactionUseCase;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.GetAccountBalanceUseCase;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.GetTransactionUseCase;
import com.nttdata.financeiro.ms2.transaction.service.application.port.in.ListTransactionsUseCase;
import com.nttdata.financeiro.ms2.transaction.service.domain.model.TransactionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Transações", description = "Registro e consulta de transações financeiras")
@SecurityRequirement(name = "basicAuth")
public class TransactionController {

    private final CreateTransactionUseCase createUseCase;
    private final GetTransactionUseCase getUseCase;
    private final ListTransactionsUseCase listUseCase;
    private final GetAccountBalanceUseCase balanceUseCase;
    private final TransactionWebMapper mapper;
    private final BasicAuthAuthenticator authenticator;

    public TransactionController(
            CreateTransactionUseCase createUseCase,
            GetTransactionUseCase getUseCase,
            ListTransactionsUseCase listUseCase,
            GetAccountBalanceUseCase balanceUseCase,
            TransactionWebMapper mapper,
            BasicAuthAuthenticator authenticator
    ) {
        this.createUseCase = createUseCase;
        this.getUseCase = getUseCase;
        this.listUseCase = listUseCase;
        this.balanceUseCase = balanceUseCase;
        this.mapper = mapper;
        this.authenticator = authenticator;
    }

    @PostMapping("/transaction")
    @Operation(summary = "Registrar uma transação")
    public ResponseEntity<CreatedTransactionResponse> criar(
            @Parameter(hidden = true)
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        UUID userId = authenticator.autenticar(authorization);
        var transaction = createUseCase.criar(mapper.toCommand(request, userId));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreatedTransactionResponse(transaction.getId(), transaction.getStatus()));
    }

    @GetMapping("/transaction/{id}")
    @Operation(summary = "Consultar uma transação pelo identificador")
    public TransactionResponse buscar(
            @Parameter(hidden = true)
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable UUID id
    ) {
        return mapper.toResponse(getUseCase.buscar(id, authenticator.autenticar(authorization)));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Listar as transações do usuário autenticado com filtros opcionais")
    public List<TransactionResponse> listar(
            @Parameter(hidden = true)
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fim,
            @RequestParam(required = false) TransactionType tipo
    ) {
        UUID userId = authenticator.autenticar(authorization);

        return listUseCase.listar(userId, inicio, fim, tipo).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/transactions/balance")
    @Operation(summary = "Consultar saldo e limites atuais no MockAPI")
    public AccountBalanceResponse obterSaldo(
            @Parameter(hidden = true)
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        UUID userId = authenticator.autenticar(authorization);
        return AccountBalanceResponse.from(balanceUseCase.consultar(userId));
    }
}
