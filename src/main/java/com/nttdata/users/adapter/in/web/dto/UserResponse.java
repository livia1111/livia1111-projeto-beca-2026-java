package com.nttdata.users.adapter.in.web.dto;

import com.nttdata.users.domain.model.Role;
import java.time.Instant;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UsuarioResponse", description = "Dados públicos do usuário, sem senha ou hash")
public record UserResponse(
        @Schema(example = "5a1eb957-5907-4ec7-90b7-f3aab2d03b19") UUID id,
        @Schema(example = "Bia Souza") String nome,
        @Schema(example = "bia@example.com") String email,
        @Schema(example = "USER") Role role,
        @Schema(example = "2026-04-12T09:00:00Z") Instant createdAt,
        @Schema(example = "2026-04-12T09:00:00Z") Instant updatedAt
) {
}
