package com.nttdata.users.adapter.in.web.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Erro", description = "Resposta padronizada para falhas da API")
public record ErrorResponse(
        @Schema(description = "Mensagem segura, sem credenciais ou detalhes internos", example = "Dados inválidos.")
        String mensagem) {
}
