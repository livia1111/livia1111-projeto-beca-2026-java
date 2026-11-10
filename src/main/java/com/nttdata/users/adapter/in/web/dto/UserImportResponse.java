package com.nttdata.users.adapter.in.web.dto;

import com.nttdata.users.application.port.in.ImportUsersResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "RelatorioImportacao", description = "Resumo e resultado de cada linha processada")
public record UserImportResponse(
        @Schema(description = "Quantidade de usuários cadastrados", example = "2") int cadastrados,
        @Schema(description = "Quantidade de linhas rejeitadas", example = "1") int rejeitados,
        @Schema(description = "Laudo por linha do arquivo") List<Linha> resultados) {

    public UserImportResponse {
        resultados = List.copyOf(resultados);
    }

    public static UserImportResponse from(ImportUsersResult result) {
        return new UserImportResponse(result.cadastrados(), result.rejeitados(),
                result.resultados().stream().map(Linha::from).toList());
    }

    @Schema(name = "ResultadoLinhaImportacao")
    public record Linha(
            @Schema(description = "Número físico da linha no arquivo, incluindo o cabeçalho", example = "3") int linha,
            @Schema(description = "E-mail normalizado quando foi possível interpretá-lo", example = "bia@example.com") String email,
            @Schema(description = "Resultado do processamento", allowableValues = {"CADASTRADO", "REJEITADO"}, example = "REJEITADO") String status,
            @Schema(description = "Código associado ao resultado: 201, 400 ou 409", example = "409") int codigo,
            @Schema(description = "Motivo ou confirmação do processamento", example = "Já existe um usuário cadastrado com este e-mail.") String mensagem) {
        static Linha from(ImportUsersResult.Linha linha) {
            return new Linha(linha.linha(), linha.email(), linha.status(), linha.codigo(), linha.mensagem());
        }
    }
}
