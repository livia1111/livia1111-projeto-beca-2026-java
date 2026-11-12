package com.nttdata.users.adapter.in.web.controller;

import com.nttdata.users.adapter.in.web.dto.CreateUserRequest;
import com.nttdata.users.adapter.in.web.dto.UpdateUserRequest;
import com.nttdata.users.adapter.in.web.dto.UserResponse;
import com.nttdata.users.adapter.in.web.dto.UserImportResponse;
import com.nttdata.users.adapter.in.web.dto.ImportInterruptedResponse;
import com.nttdata.users.adapter.in.web.error.ErrorResponse;
import com.nttdata.users.adapter.in.web.error.SwaggerExamples;
import com.nttdata.users.adapter.in.web.mapper.UserWebMapper;
import com.nttdata.users.adapter.in.security.AuthenticatedUserDetails;
import com.nttdata.users.domain.model.Role;
import com.nttdata.users.application.port.in.CreateUserUseCase;
import com.nttdata.users.application.port.in.ImportUsersUseCase;
import com.nttdata.users.application.port.in.ManageUserUseCase;
import com.nttdata.users.application.exception.BusinessValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "Usuários", description = "CRUD de usuários")
public class UserController {

    private final CreateUserUseCase useCase;
    private final ManageUserUseCase manage;
    private final UserWebMapper mapper;
    private final ImportUsersUseCase importacao;

    public UserController(CreateUserUseCase useCase, ManageUserUseCase manage, UserWebMapper mapper,
                          ImportUsersUseCase importacao) {
        this.useCase = useCase;
        this.manage = manage;
        this.mapper = mapper;
        this.importacao = importacao;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar usuário",
        description = "Cria um novo usuário. Requer HTTP Basic com perfil ADMIN.",
        security = @SecurityRequirement(name = "basicAuth"),
        responses = {
            @ApiResponse(responseCode = "201", description = "Usuário criado",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "validacao", value = SwaggerExamples.VALIDATION))),
            @ApiResponse(responseCode = "401", description = "Credenciais HTTP Basic ausentes ou inválidas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "nao-autenticado", value = SwaggerExamples.UNAUTHORIZED))),
            @ApiResponse(responseCode = "403", description = "Apenas ADMIN pode cadastrar usuários",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "sem-permissao", value = SwaggerExamples.FORBIDDEN))),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "email-duplicado", value = SwaggerExamples.EMAIL_CONFLICT)))
        })
    public UserResponse criar(@Valid @RequestBody CreateUserRequest request) {
        return mapper.toResponse(useCase.criar(mapper.toCommand(request)));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Importar usuários por CSV",
            description = "Requer HTTP Basic com perfil ADMIN. Recebe arquivo de texto CSV UTF-8 "
                    + "como multipart/form-data no campo 'file', separado por ponto e vírgula. Cabeçalho: "
                    + "nome;email;role;senha. Processa até 1.000 linhas e retorna e-mail e resultado por linha.",
            security = @SecurityRequirement(name = "basicAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Arquivo processado; confira o resultado de cada linha",
                            content = @Content(schema = @Schema(implementation = UserImportResponse.class),
                                    examples = @ExampleObject(name = "relatorio-por-linha", value = SwaggerExamples.IMPORT_REPORT))),
                    @ApiResponse(responseCode = "400", description = "Arquivo inválido ou limite de linhas excedido; consulte o corpo para a mensagem ou relatório parcial",
                            content = @Content(schema = @Schema(oneOf = {ErrorResponse.class, ImportInterruptedResponse.class}),
                                    examples = {
                                            @ExampleObject(name = "arquivo-invalido", value = "{\"mensagem\":\"Cabeçalho inválido. Use nome;email;role;senha.\"}"),
                                            @ExampleObject(name = "relatorio-parcial", value = SwaggerExamples.PARTIAL_IMPORT)
                                    })),
                    @ApiResponse(responseCode = "401", description = "Credenciais HTTP Basic ausentes ou inválidas",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(name = "nao-autenticado", value = SwaggerExamples.UNAUTHORIZED))),
                    @ApiResponse(responseCode = "403", description = "Apenas ADMIN pode importar usuários",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(name = "sem-permissao", value = SwaggerExamples.FORBIDDEN))),
                    @ApiResponse(responseCode = "413", description = "Arquivo acima do limite permitido",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(name = "arquivo-grande", value = "{\"mensagem\":\"O arquivo enviado excede o tamanho permitido.\"}"))),
                    @ApiResponse(responseCode = "500", description = "Falha ao ler ou persistir; consulte o relatório parcial",
                            content = @Content(schema = @Schema(implementation = ImportInterruptedResponse.class),
                                    examples = @ExampleObject(name = "falha-com-parcial", value = SwaggerExamples.PARTIAL_IMPORT)))
            })
    public ResponseEntity<UserImportResponse> importar(
            @Parameter(description = "Arquivo TXT/CSV UTF-8 delimitado por ponto e vírgula.", required = true,
                    schema = @Schema(type = "string", format = "binary"))
            @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BusinessValidationException("O arquivo está vazio.");
        }
        try (InputStream stream = file.getInputStream()) {
            UserImportResponse result = UserImportResponse.from(importacao.importar(stream));
            return ResponseEntity.ok(result);
        }
    }

    @GetMapping
    @Operation(summary = "Listar usuários",
        description = "Retorna a lista de todos os usuários. Requer HTTP Basic com perfil ADMIN.",
        security = @SecurityRequirement(name = "basicAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários",
                content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Credenciais HTTP Basic ausentes ou inválidas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "nao-autenticado", value = SwaggerExamples.UNAUTHORIZED))),
            @ApiResponse(responseCode = "403", description = "Apenas ADMIN pode listar usuários",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "sem-permissao", value = SwaggerExamples.FORBIDDEN)))
        })
    public List<UserResponse> listar() {
        return manage.listar().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário",
        description = "Usuários autenticados podem consultar o próprio perfil; ADMIN pode consultar qualquer usuário.",
        security = @SecurityRequirement(name = "basicAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais HTTP Basic ausentes ou inválidas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "nao-autenticado", value = SwaggerExamples.UNAUTHORIZED))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "nao-encontrado", value = SwaggerExamples.NOT_FOUND))),
            @ApiResponse(responseCode = "403", description = "Usuário comum só pode consultar o próprio perfil",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "sem-permissao", value = SwaggerExamples.FORBIDDEN)))
        })
    public UserResponse buscar(@AuthenticationPrincipal AuthenticatedUserDetails usuario, @PathVariable UUID id) {
        exigirProprioOuAdmin(usuario, id);
        return mapper.toResponse(manage.buscar(id));
    }

     @GetMapping("/{id}/exists")
    @Operation(summary = "Verificar se usuário existe", 
               description = "Endpoint aberto para comunicação entre microsserviços (usado pelo MS2 para validar destinatários de transferência).")
    public ResponseEntity<Void> verificarExistencia(@PathVariable UUID id) {
        try {
            manage.buscar(id);
            return ResponseEntity.ok().build();
        } catch (com.nttdata.users.application.exception.UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    // =========================================================================

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário",
        description = "Atualiza nome, e-mail e senha. O próprio usuário ou um ADMIN pode realizar a operação.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "validacao", value = SwaggerExamples.VALIDATION))),
            @ApiResponse(responseCode = "401", description = "Credenciais HTTP Basic ausentes ou inválidas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "nao-autenticado", value = SwaggerExamples.UNAUTHORIZED))),
            @ApiResponse(responseCode = "403", description = "Usuário comum só pode alterar a própria conta",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "conta-de-outro", value = SwaggerExamples.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "nao-encontrado", value = SwaggerExamples.NOT_FOUND))),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "email-duplicado", value = SwaggerExamples.EMAIL_CONFLICT)))
        })
    public UserResponse atualizar(@AuthenticationPrincipal AuthenticatedUserDetails usuario,
                                  @PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        exigirProprioOuAdmin(usuario, id);
        return mapper.toResponse(manage.atualizar(id, mapper.toCommand(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir usuário",
        description = "Remove o usuário. O próprio usuário ou um ADMIN pode realizar a operação.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Usuário excluído"),
            @ApiResponse(responseCode = "401", description = "Credenciais HTTP Basic ausentes ou inválidas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "nao-autenticado", value = SwaggerExamples.UNAUTHORIZED))),
            @ApiResponse(responseCode = "403", description = "Usuário comum só pode excluir a própria conta",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "conta-de-outro", value = SwaggerExamples.FORBIDDEN))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "nao-encontrado", value = SwaggerExamples.NOT_FOUND)))
        })
    public void excluir(@AuthenticationPrincipal AuthenticatedUserDetails usuario, @PathVariable UUID id) {
        exigirProprioOuAdmin(usuario, id);
        manage.excluir(id);
    }

    private void exigirProprioOuAdmin(AuthenticatedUserDetails usuario, UUID id) {
        if (usuario == null || (!usuario.id().equals(id) && usuario.role() != Role.ADMIN)) {
            throw new AccessDeniedException("Usuário comum só pode acessar a própria conta.");
        }
    }
}