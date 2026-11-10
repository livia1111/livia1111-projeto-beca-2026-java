package com.nttdata.users.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AtualizarUsuarioRequest", description = "Dados editáveis da própria conta")
public record UpdateUserRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
        @Schema(example = "Bia Souza") String nome,
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres.")
        @Schema(example = "bia.souza@example.com") String email,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @NotBlank(message = "A senha é obrigatória.")
        @Schema(accessMode = Schema.AccessMode.WRITE_ONLY, example = "SenhaBia2026")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.") String senha
) {
    public UpdateUserRequest {
        nome = nome == null ? null : nome.strip();
        email = email == null ? null : email.strip();
    }

    @Override
    public String toString() { return "UpdateUserRequest[conteudo omitido]"; }
}
