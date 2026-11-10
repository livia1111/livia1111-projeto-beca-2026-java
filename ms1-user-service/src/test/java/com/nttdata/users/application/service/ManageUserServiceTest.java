package com.nttdata.users.application.service;

import com.nttdata.users.application.exception.EmailAlreadyExistsException;
import com.nttdata.users.application.exception.BusinessValidationException;
import com.nttdata.users.application.exception.UserNotFoundException;
import com.nttdata.users.application.port.in.UpdateUserCommand;
import com.nttdata.users.application.port.out.PasswordHasher;
import com.nttdata.users.application.port.out.UserRepository;
import com.nttdata.users.domain.model.Role;
import com.nttdata.users.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageUserServiceTest {
    private static final UUID BIA_ID = UUID.fromString("5a1eb957-5907-4ec7-90b7-f3aab2d03b19");
    private static final UUID CAIO_ID = UUID.fromString("de16b2a4-69c4-4db0-8e4a-df9d3d237aa4");
    private static final Instant CREATED = Instant.parse("2026-04-12T09:00:00Z");

    @Mock private UserRepository repository;
    @Mock private PasswordHasher hasher;
    private ManageUserService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-04-13T09:00:00Z"), ZoneOffset.UTC);
        service = new ManageUserService(repository, hasher, clock);
    }

    private static User bia() {
        return User.reconstituir(BIA_ID, "Bia", "bia@example.com", "hash-antigo", Role.USER, CREATED, CREATED);
    }

    @Test
    void listar_deveRetornarTodosOsUsuariosDoRepositorio() {
        List<User> usuarios = List.of(bia());
        when(repository.listar()).thenReturn(usuarios);

        assertThat(service.listar()).isSameAs(usuarios);
    }

    @Test
    void buscar_deveRetornarUsuarioExistente() {
        User usuario = bia();
        when(repository.buscarPorId(BIA_ID)).thenReturn(Optional.of(usuario));

        assertThat(service.buscar(BIA_ID)).isSameAs(usuario);
    }

    @Test
    void buscar_deveFalharQuandoUsuarioNaoExiste() {
        when(repository.buscarPorId(CAIO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(CAIO_ID)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void atualizar_deveNormalizarEmailPreservarPerfilEAtualizarHashEDatas() {
        when(repository.buscarPorId(BIA_ID)).thenReturn(Optional.of(bia()));
        when(repository.buscarIdPorEmail("bia.nova@example.com")).thenReturn(Optional.empty());
        when(hasher.hash("SenhaBia2026")).thenReturn("hash-novo");
        when(repository.atualizar(any(User.class))).thenReturn(true);

        User atualizada = service.atualizar(BIA_ID,
                new UpdateUserCommand(" Bia Souza ", " BIA.Nova@Example.com ", "SenhaBia2026"));

        assertThat(atualizada.getNome()).isEqualTo("Bia Souza");
        assertThat(atualizada.getEmail()).isEqualTo("bia.nova@example.com");
        assertThat(atualizada.getSenha()).isEqualTo("hash-novo");
        assertThat(atualizada.getRole()).isEqualTo(Role.USER);
        assertThat(atualizada.getCreatedAt()).isEqualTo(CREATED);
        assertThat(atualizada.getUpdatedAt()).isAfter(CREATED);
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(repository).atualizar(saved.capture());
        assertThat(saved.getValue()).isEqualTo(atualizada);
    }

    @Test
    void atualizar_devePermitirMesmoEmailNormalizadoDoProprioUsuario() {
        when(repository.buscarPorId(BIA_ID)).thenReturn(Optional.of(bia()));
        when(repository.buscarIdPorEmail("bia@example.com")).thenReturn(Optional.of(BIA_ID));
        when(hasher.hash("SenhaBia2026")).thenReturn("hash-novo");
        when(repository.atualizar(any(User.class))).thenReturn(true);

        User atualizada = service.atualizar(BIA_ID,
                new UpdateUserCommand("Bia", " BIA@EXAMPLE.COM ", "SenhaBia2026"));

        assertThat(atualizada.getEmail()).isEqualTo("bia@example.com");
    }

    @Test
    void atualizar_deveRejeitarEmailDeOutraConta() {
        when(repository.buscarPorId(BIA_ID)).thenReturn(Optional.of(bia()));
        when(repository.buscarIdPorEmail("caio@example.com")).thenReturn(Optional.of(CAIO_ID));

        assertThatThrownBy(() -> service.atualizar(BIA_ID,
                new UpdateUserCommand("Bia", "caio@example.com", "SenhaBia2026")))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(hasher, never()).hash(any());
        verify(repository, never()).atualizar(any());
    }

    @Test
    void atualizar_deveFalharSeUsuarioNaoExiste() {
        when(repository.buscarPorId(CAIO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(CAIO_ID,
                new UpdateUserCommand("Caio", "caio@example.com", "SenhaCaio2026")))
                .isInstanceOf(UserNotFoundException.class);
        verify(repository, never()).atualizar(any());
    }

    @Test
    void atualizar_deveFalharSeRegistroForRemovidoAntesDaGravacao() {
        when(repository.buscarPorId(BIA_ID)).thenReturn(Optional.of(bia()));
        when(repository.buscarIdPorEmail("bia.nova@example.com")).thenReturn(Optional.empty());
        when(hasher.hash("SenhaBia2026")).thenReturn("hash-novo");
        when(repository.atualizar(any(User.class))).thenReturn(false);

        assertThatThrownBy(() -> service.atualizar(BIA_ID,
                new UpdateUserCommand("Bia", "bia.nova@example.com", "SenhaBia2026")))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void atualizar_deveRejeitarSenhaAcimaDoLimiteBCryptEmBytes() {
        when(repository.buscarPorId(BIA_ID)).thenReturn(Optional.of(bia()));
        String senhaComMaisDe72Bytes = "😀".repeat(19);

        assertThatThrownBy(() -> service.atualizar(BIA_ID,
                new UpdateUserCommand("Bia", "bia@example.com", senhaComMaisDe72Bytes)))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("72 bytes");
        verify(hasher, never()).hash(any());
        verify(repository, never()).atualizar(any());
    }

    @Test
    void excluir_deveRemoverUsuarioExistente() {
        when(repository.buscarPorId(BIA_ID)).thenReturn(Optional.of(bia()));
        when(repository.excluir(BIA_ID)).thenReturn(true);

        service.excluir(BIA_ID);

        verify(repository).excluir(BIA_ID);
    }

    @Test
    void excluir_deveFalharSeUsuarioNaoExiste() {
        when(repository.buscarPorId(CAIO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(CAIO_ID)).isInstanceOf(UserNotFoundException.class);
        verify(repository, never()).excluir(any());
    }

    @Test
    void excluir_deveFalharSeRegistroForRemovidoAntesDaExclusao() {
        when(repository.buscarPorId(BIA_ID)).thenReturn(Optional.of(bia()));
        when(repository.excluir(BIA_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.excluir(BIA_ID)).isInstanceOf(UserNotFoundException.class);
    }
}
