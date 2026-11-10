package com.nttdata.users.adapter.in.web.controller;

import com.nttdata.users.adapter.in.security.AuthenticatedUserDetails;
import com.nttdata.users.adapter.in.web.error.ErrorResponse;
import com.nttdata.users.adapter.in.web.error.SwaggerExamples;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tag(name = "Autenticação")
public class AuthController {
    @PostMapping("/auth/login")
    @Operation(summary = "Validar credenciais HTTP Basic e obter o ID do usuário",
            description = "Envie e-mail e senha no cabeçalho HTTP Basic. A resposta contém o ID fixo da conta.",
            security = @SecurityRequirement(name = "basicAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Credenciais válidas; UUID do usuário retornado",
                            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(name = "credenciais-invalidas", value = SwaggerExamples.UNAUTHORIZED)))
            })
    public LoginResponse login(Authentication authentication) {
        var user = (AuthenticatedUserDetails) authentication.getPrincipal();
        return new LoginResponse(user.id());
    }

    public record LoginResponse(@Schema(description = "UUID fixo do usuário autenticado") UUID id) { }
}
