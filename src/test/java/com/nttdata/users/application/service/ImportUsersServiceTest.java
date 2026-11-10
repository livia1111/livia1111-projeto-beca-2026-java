package com.nttdata.users.application.service;

import com.nttdata.users.adapter.out.csv.UserCsvReaderAdapter;
import com.nttdata.users.application.exception.EmailAlreadyExistsException;
import com.nttdata.users.application.exception.ImportInterruptedException;
import com.nttdata.users.application.port.in.CreateUserUseCase;
import com.nttdata.users.application.port.in.ImportUsersResult;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImportUsersServiceTest {
    private static final ValidatorFactory VALIDATORS = Validation.buildDefaultValidatorFactory();
    private CreateUserUseCase createUser;
    private ImportUsersService service;

    @BeforeEach
    void setUp() {
        createUser = mock(CreateUserUseCase.class);
        service = new ImportUsersService(new UserCsvReaderAdapter(VALIDATORS.getValidator()), createUser);
    }

    @AfterAll
    static void closeValidator() { VALIDATORS.close(); }

    private static InputStream txt(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void importar_deveCadastrarLinhasEInformarEmailNoLaudo() {
        ImportUsersResult result = service.importar(txt("""
                nome;email;role;senha
                Bia;bia@example.com;USER;SenhaBia2026
                Caio;caio@example.com;ADMIN;SenhaCaio2026
                """));

        assertThat(result.cadastrados()).isEqualTo(2);
        assertThat(result.rejeitados()).isZero();
        assertThat(result.resultados()).extracting(ImportUsersResult.Linha::email)
                .containsExactly("bia@example.com", "caio@example.com");
        verify(createUser, times(2)).criar(any());
    }

    @Test
    void importar_deveRegistrarEmailsRejeitadosEContinuarAsOutrasLinhas() {
        when(createUser.criar(any())).thenThrow(new EmailAlreadyExistsException())
                .thenReturn(null);

        ImportUsersResult result = service.importar(txt("""
                nome;email;role;senha
                Bia;BIA@example.com;USER;SenhaBia2026
                Duda;email-invalido;USER;SenhaDuda2026
                Eva;eva@example.com;USER;SenhaEva2026
                """));

        assertThat(result.cadastrados()).isEqualTo(1);
        assertThat(result.rejeitados()).isEqualTo(2);
        assertThat(result.resultados()).extracting(ImportUsersResult.Linha::email)
                .containsExactly("bia@example.com", "email-invalido", "eva@example.com");
        assertThat(result.resultados()).extracting(ImportUsersResult.Linha::codigo)
                .containsExactly(409, 400, 201);
        verify(createUser, times(2)).criar(any());
    }

    @Test
    void importar_deveRejeitarCabecalhoComOrdemDiferente() {
        assertThatThrownBy(() -> service.importar(txt(
                "nome;email;senha;role\nDuda;duda@example.com;SenhaDuda2026;USER\n")))
                .isInstanceOf(ImportInterruptedException.class)
                .hasMessageContaining("Cabeçalho inválido");
        verify(createUser, never()).criar(any());
    }
}
