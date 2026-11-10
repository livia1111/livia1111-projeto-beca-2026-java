package com.nttdata.users.application.service;

import com.nttdata.users.application.exception.EmailAlreadyExistsException;
import com.nttdata.users.application.port.in.CreateUserCommand;
import com.nttdata.users.application.port.out.PasswordHasher;
import com.nttdata.users.application.port.out.UserRepository;
import com.nttdata.users.domain.model.Role;
import com.nttdata.users.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {
    @Mock private UserRepository repository;
    @Mock private PasswordHasher hasher;
    private CreateUserService service;

    @BeforeEach
    void setUp() {
        service = new CreateUserService(repository, hasher,
                Clock.fixed(Instant.parse("2026-02-03T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void criar_deveNormalizarEmailEArmazenarHash() {
        when(repository.buscarEmail("bia.souza@example.com")).thenReturn(Optional.empty());
        when(hasher.hash("SenhaBia2026")).thenReturn("hash-seguro");
        when(repository.salvar(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User criada = service.criar(new CreateUserCommand("Bia Souza", "  Bia.Souza@Example.COM  ",
                "SenhaBia2026", Role.USER));

        assertThat(criada.getEmail()).isEqualTo("bia.souza@example.com");
        assertThat(criada.getSenha()).isEqualTo("hash-seguro");
        assertThat(criada.getId()).isNotNull();
        verify(repository).buscarEmail("bia.souza@example.com");
        verify(repository).salvar(criada);
        verify(hasher).hash("SenhaBia2026");
    }

    @Test
    void criar_deveRejeitarEmailDuplicadoAntesDeGerarHash() {
        when(repository.buscarEmail("bia.souza@example.com")).thenReturn(Optional.of("bia.souza@example.com"));

        assertThatThrownBy(() -> service.criar(new CreateUserCommand("Bia", "BIA.SOUZA@example.com",
                "SenhaBia2026", Role.USER))).isInstanceOf(EmailAlreadyExistsException.class);

        verify(hasher, never()).hash(any());
        verify(repository, never()).salvar(any());
    }
}
