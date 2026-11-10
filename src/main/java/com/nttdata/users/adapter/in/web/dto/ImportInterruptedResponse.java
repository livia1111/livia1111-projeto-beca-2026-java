package com.nttdata.users.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ImportacaoInterrompida", description = "Falha que interrompeu o arquivo após possíveis gravações parciais")
public record ImportInterruptedResponse(
        @Schema(example = "Importação interrompida; consulte o relatório parcial antes de reenviar.") String mensagem,
        UserImportResponse parcial) {
}
