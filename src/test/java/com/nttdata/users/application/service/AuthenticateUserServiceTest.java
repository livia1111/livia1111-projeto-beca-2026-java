package com.nttdata.users.application.service;

import com.nttdata.users.application.exception.InvalidCredentialsException;
import com.nttdata.users.application.port.out.PasswordHasher;
import com.nttdata.users.application.port.out.UserRepository;
import com.nttdata.users.domain.model.Role;
import com.nttdata.users.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserServiceTest {
    private static final String EMAIL = "bia@example.com";
    private static final String RAW_PASSWORD = "SenhaBia2026";
    private static final String PASSWORD_HASH = "hash-senha-bia";
    private static final UUID ID = UUID.fromString("58e724d3-6b75-49f1-a692-58ade0e13fb1");

    @Mock private UserRepository repository;
    @Mock private PasswordHasher hasher;
    private AuthenticateUserService service;

    @BeforeEach
    void setUp() {
        service = new AuthenticateUserService(repository, hasher);
    }

    private static User bia() {
        Instant created = Instant.parse("2026-05-02T12:00:00Z");
        return User.reconstituir(ID, "Bia Souza", EMAIL, PASSWORD_HASH, Role.USER, created, created);
    }

    @Test
    void autenticar_deveRetornarUsuarioComCredenciaisCorretasENormalizarEmail() {
        User user = bia();
        when(repository.buscarPorEmailCompleto(EMAIL)).thenReturn(Optional.of(user));
        when(hasher.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);

        assertThat(service.autenticar(" BIA@EXAMPLE.COM ", RAW_PASSWORD)).isSameAs(user);

        verify(repository).buscarPorEmailCompleto(EMAIL);
        verify(hasher).matches(RAW_PASSWORD, PASSWORD_HASH);
    }

    @Test
    void autenticar_deveRejeitarSenhaErradaComMensagemGenerica() {
        when(repository.buscarPorEmailCompleto(EMAIL)).thenReturn(Optional.of(bia()));
        when(hasher.matches("senha-errada", PASSWORD_HASH)).thenReturn(false);

        assertThatThrownBy(() -> service.autenticar(EMAIL, "senha-errada"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("E-mail ou senha incorretos.");
    }

    @Test
    void autenticar_deveUsarAMesmaMensagemQuandoEmailNaoExiste() {
        when(repository.buscarPorEmailCompleto("ninguem@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.autenticar("ninguem@example.com", RAW_PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("E-mail ou senha incorretos.");
        verifyNoInteractions(hasher);
    }

    @Test
    void autenticar_deveRejeitarCredenciaisAusentesSemConsultarRepositorio() {
        assertThatThrownBy(() -> service.autenticar(" ", RAW_PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("E-mail ou senha incorretos.");
        verify(repository, never()).buscarPorEmailCompleto(org.mockito.ArgumentMatchers.anyString());
    }
}
