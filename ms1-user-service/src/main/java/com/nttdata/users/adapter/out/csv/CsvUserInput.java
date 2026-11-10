package com.nttdata.users.adapter.out.csv;

import com.nttdata.users.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Modelo de validação interno do adaptador CSV, independente dos DTOs HTTP. */
record CsvUserInput(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
        String senha,

        @NotNull(message = "O perfil do usuário é obrigatório.")
        Role role) {

    CsvUserInput {
        nome = nome == null ? null : nome.strip();
        email = email == null ? null : email.strip();
    }
}
