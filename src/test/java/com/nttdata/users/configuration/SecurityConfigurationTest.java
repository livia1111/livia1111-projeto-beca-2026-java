package com.nttdata.users.configuration;

import com.nttdata.users.adapter.in.web.controller.UserController;
import com.nttdata.users.adapter.in.web.controller.AuthController;
import com.nttdata.users.adapter.in.web.mapper.UserWebMapper;
import com.nttdata.users.application.port.in.CreateUserUseCase;
import com.nttdata.users.application.port.in.ImportUsersUseCase;
import com.nttdata.users.application.port.in.ManageUserUseCase;
import com.nttdata.users.application.port.in.UpdateUserCommand;
import com.nttdata.users.application.port.in.AuthenticateUserUseCase;
import com.nttdata.users.domain.model.Role;
import com.nttdata.users.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserController.class, AuthController.class})
@ContextConfiguration(classes = SecurityConfigurationTest.TestApplication.class)
class SecurityConfigurationTest {
    private static final String ADMIN_EMAIL = "admin.qa@example.com";
    private static final String USER_EMAIL = "bia.qa@example.com";
    private static final String PASSWORD = "SenhaQa2026";
    private static final UUID USER_ID = UUID.fromString("256e2c03-7165-4764-b6e6-99fae10373ec");

    @Autowired private MockMvc mockMvc;
    @MockitoBean private AuthenticateUserUseCase authenticate;
    @MockitoBean private CreateUserUseCase createUser;
    @MockitoBean private ManageUserUseCase manage;
    @MockitoBean private ImportUsersUseCase importer;

    private final UUID adminId = UUID.fromString("bf52ec31-c1c4-4a6e-85ae-0d3a54de83b1");

    @org.springframework.boot.SpringBootConfiguration
    @org.springframework.boot.autoconfigure.EnableAutoConfiguration
    @Import({SecurityConfiguration.class, UserWebMapper.class, UserController.class, AuthController.class})
    static class TestApplication { }

    @BeforeEach
    void setUpUsers() {
        when(authenticate.autenticar(ADMIN_EMAIL, PASSWORD)).thenReturn(user(adminId, ADMIN_EMAIL, Role.ADMIN));
        when(authenticate.autenticar(USER_EMAIL, PASSWORD)).thenReturn(user(USER_ID, USER_EMAIL, Role.USER));
    }

    private User user(UUID id, String email, Role role) {
        Instant now = Instant.parse("2026-03-04T10:00:00Z");
        return User.reconstituir(id, "Conta de Teste", email, "hash-senha", role, now, now);
    }

    @Test
    void semCredenciais_deveNegarCadastro() throws Exception {
        mockMvc.perform(post("/users").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void senhaBasicIncorreta_deveRetornar401JsonSemExecutarCasoDeUso() throws Exception {
        when(authenticate.autenticar(USER_EMAIL, "senha-errada"))
                .thenThrow(new com.nttdata.users.application.exception.InvalidCredentialsException());
        mockMvc.perform(get("/users").with(httpBasic(USER_EMAIL, "senha-errada")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Credenciais HTTP Basic ausentes ou inválidas."));
        verifyNoInteractions(manage);
    }

    @Test
    void emailInexistente_eSenhaIncorretaDevemTerResposta401Generica() throws Exception {
        when(authenticate.autenticar(USER_EMAIL, "senha-errada"))
                .thenThrow(new com.nttdata.users.application.exception.InvalidCredentialsException());
        when(authenticate.autenticar("ninguem.qa@example.com", PASSWORD))
                .thenThrow(new com.nttdata.users.application.exception.InvalidCredentialsException());

        mockMvc.perform(get("/users").with(httpBasic(USER_EMAIL, "senha-errada")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Credenciais HTTP Basic ausentes ou inválidas."));
        mockMvc.perform(get("/users").with(httpBasic("ninguem.qa@example.com", PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Credenciais HTTP Basic ausentes ou inválidas."));
        verifyNoInteractions(manage);
    }

    @Test
    void usuarioComum_naoPodeCadastrar() throws Exception {
        mockMvc.perform(post("/users").with(httpBasic(USER_EMAIL, PASSWORD))
                        .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorAutenticadoPodeAcessarCadastro() throws Exception {
        // O corpo inválido deve chegar à validação do controller, provando que o filtro liberou o ADMIN.
        mockMvc.perform(post("/users").with(httpBasic(ADMIN_EMAIL, PASSWORD))
                        .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginBasic_devolveUuidFixoDoUsuario() throws Exception {
        mockMvc.perform(post("/auth/login").with(httpBasic(ADMIN_EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(adminId.toString()));
    }

    @Test
    void usuarioNaoPodeAtualizarOutraConta() throws Exception {
        mockMvc.perform(put("/users/{id}", adminId).with(httpBasic(USER_EMAIL, PASSWORD))
                        .contentType("application/json")
                        .content("{\"nome\":\"Bia\",\"email\":\"bia.nova@example.com\",\"senha\":\"SenhaNova2026\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(manage);
    }

    @Test
    void usuarioNaoPodeExcluirOutraConta() throws Exception {
        mockMvc.perform(delete("/users/{id}", adminId).with(httpBasic(USER_EMAIL, PASSWORD)))
                .andExpect(status().isForbidden());
        verifyNoInteractions(manage);
    }

    @Test
    void usuarioPodeAtualizarPropriaConta() throws Exception {
        when(manage.atualizar(org.mockito.ArgumentMatchers.eq(USER_ID), org.mockito.ArgumentMatchers.any()))
                .thenReturn(user(USER_ID, USER_EMAIL, Role.USER));

        mockMvc.perform(put("/users/{id}", USER_ID).with(httpBasic(USER_EMAIL, PASSWORD))
                        .contentType("application/json")
                        .content("{\"nome\":\"Bia Souza\",\"email\":\"bia.souza@example.com\",\"senha\":\"SenhaNova2026\"}"))
                .andExpect(status().isOk());

        verify(manage).atualizar(org.mockito.ArgumentMatchers.eq(USER_ID),
                org.mockito.ArgumentMatchers.eq(new UpdateUserCommand("Bia Souza", "bia.souza@example.com", "SenhaNova2026")));
    }

    @Test
    void usuarioPodeExcluirPropriaConta() throws Exception {
        mockMvc.perform(delete("/users/{id}", USER_ID).with(httpBasic(USER_EMAIL, PASSWORD)))
                .andExpect(status().isNoContent());
        verify(manage).excluir(USER_ID);
    }
}
