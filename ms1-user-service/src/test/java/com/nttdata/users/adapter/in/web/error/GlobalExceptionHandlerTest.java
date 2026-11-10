package com.nttdata.users.adapter.in.web.error;

import com.nttdata.users.adapter.in.web.dto.CreateUserRequest;
import com.nttdata.users.adapter.out.persistence.UserPersistenceExceptionTranslator;
import com.nttdata.users.application.exception.BusinessValidationException;
import com.nttdata.users.application.exception.EmailAlreadyExistsException;
import com.nttdata.users.application.exception.UserNotFoundException;
import jakarta.validation.Valid;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ErrorTestController())
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void duplicateEmailIs409() throws Exception {
        mvc.perform(get("/test/error/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").doesNotExist())
                .andExpect(jsonPath("$.codigo").doesNotExist())
                .andExpect(jsonPath("$.mensagem").value(
                        "Já existe um usuário cadastrado com este e-mail."));
    }

    @Test
    void databaseDuplicateHasSameResponseAsBusinessDuplicate() throws Exception {
        String expected = mvc.perform(get("/test/error/duplicate"))
                .andReturn().getResponse().getContentAsString();
        mvc.perform(get("/test/error/database-duplicate"))
                .andExpect(status().isConflict())
                .andExpect(content().json(expected));
    }

    @Test
    void otherIntegrityConflictDoesNotPretendToBeDuplicateEmail() throws Exception {
        mvc.perform(get("/test/error/integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("A operação viola uma restrição dos dados."))
                .andExpect(content().string(not(containsString("SQL_INTERNO"))));
    }

    @Test
    void missingUserIs404() throws Exception {
        mvc.perform(get("/test/error/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado."));
    }

    @Test
    void businessValidationIs400() throws Exception {
        mvc.perform(get("/test/error/business"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("A senha excede 72 bytes."));
    }

    @Test
    void validationIncludesAllMessagesWithoutEchoingPassword() throws Exception {
        mvc.perform(post("/test/input").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"", "email":"invalido", "senha":"x7!", "role":null}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(containsString("O nome é obrigatório.")))
                .andExpect(jsonPath("$.mensagem").value(containsString("Informe um e-mail válido.")))
                .andExpect(jsonPath("$.mensagem").value(containsString("A senha deve ter entre 8 e 72 caracteres.")))
                .andExpect(jsonPath("$.mensagem").value(containsString("O perfil do usuário é obrigatório.")))
                .andExpect(content().string(not(containsString("x7!"))));
    }

    @Test
    void objectLevelValidationDoesNotRequireFieldErrors() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "input");
        binding.addError(new ObjectError("input", "Combinação de campos inválida."));
        MethodParameter parameter = new MethodParameter(
                ErrorTestController.class.getMethod("input", CreateUserRequest.class), 0);
        var exception = new MethodArgumentNotValidException(parameter, binding);
        var response = new GlobalExceptionHandler().handleMethodArgumentNotValid(
                exception, new HttpHeaders(), HttpStatus.BAD_REQUEST, null);
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(((ErrorResponse) response.getBody()).mensagem())
                .isEqualTo("Combinação de campos inválida.");
    }

    @Test
    void malformedJsonHasStandardResponse() throws Exception {
        mvc.perform(post("/test/input").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("A requisição contém dados ausentes, malformados ou incompatíveis."));
    }

    @Test
    void invalidRoleDoesNotLeakInput() throws Exception {
        mvc.perform(post("/test/input").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Maria", "email":"maria@example.com", "senha":"Segredo123!", "role":"ROOT"}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("A requisição contém dados ausentes, malformados ou incompatíveis."))
                .andExpect(content().string(not(containsString("Segredo123!"))));
    }

    @Test
    void methodNotAllowedPreservesAllowHeader() throws Exception {
        mvc.perform(put("/test/input"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", containsString("POST")))
                .andExpect(jsonPath("$.mensagem").value("Método HTTP não permitido para este recurso."));
    }

    @Test
    void unsupportedContentTypeIs415() throws Exception {
        mvc.perform(post("/test/input").contentType(MediaType.TEXT_PLAIN).content("texto"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.mensagem").value("O tipo de conteúdo enviado não é suportado."));
    }

    @Test
    void unexpectedErrorIs500AndDoesNotExposeTechnicalMessage() throws Exception {
        mvc.perform(get("/test/error/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value("Não foi possível concluir a operação."))
                .andExpect(content().string(not(containsString("SEGREDO_INTERNO"))));
    }

    @Test
    void unknownRouteIs404WithStandardResponse() throws Exception {
        mvc.perform(get("/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Recurso não encontrado."));
    }

    /** Controller exclusivo de teste; nao cria endpoints na aplicacao. */
    @RestController
    static class ErrorTestController {
        @PostMapping("/test/input")
        public void input(@Valid @RequestBody CreateUserRequest request) {
        }

        @GetMapping("/test/error/{kind}")
        public void error(@PathVariable String kind) {
            switch (kind) {
                case "duplicate" -> throw new EmailAlreadyExistsException();
                case "database-duplicate" -> throw databaseFailure("uk_users_email");
                case "integrity" -> throw databaseFailure("ck_users_role");
                case "not-found" -> throw new UserNotFoundException();
                case "business" -> throw new BusinessValidationException("A senha excede 72 bytes.");
                default -> throw new IllegalStateException("SEGREDO_INTERNO");
            }
        }

        private RuntimeException databaseFailure(String constraint) {
            var hibernateError = new ConstraintViolationException(
                    "SQL_INTERNO", new SQLException("SQL_INTERNO"), constraint);
            var springError = new DataIntegrityViolationException("SQL_INTERNO", hibernateError);
            return new UserPersistenceExceptionTranslator().translate(springError);
        }
    }
}
