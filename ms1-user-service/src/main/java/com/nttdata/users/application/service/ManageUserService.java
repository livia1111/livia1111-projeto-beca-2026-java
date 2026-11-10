package com.nttdata.users.application.service;

import com.nttdata.users.application.exception.BusinessValidationException;
import com.nttdata.users.application.exception.EmailAlreadyExistsException;
import com.nttdata.users.application.exception.UserNotFoundException;
import com.nttdata.users.application.port.in.UpdateUserCommand;
import com.nttdata.users.application.port.in.ManageUserUseCase;
import com.nttdata.users.application.port.out.PasswordHasher;
import com.nttdata.users.application.port.out.UserRepository;
import com.nttdata.users.domain.model.User;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ManageUserService implements ManageUserUseCase {
    private final UserRepository repository;
    private final PasswordHasher hasher;
    private final Clock clock;

    public ManageUserService(UserRepository repository, PasswordHasher hasher, Clock clock) {
        this.repository = repository;
        this.hasher = hasher;
        this.clock = clock;
    }

    @Override
    public List<User> listar() {
        return repository.listar();
    }

    @Override
    public User buscar(UUID id) {
        return repository.buscarPorId(id)
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    public User atualizar(UUID id, UpdateUserCommand command) {
        User atual = buscar(id);
        String email = command.email().strip().toLowerCase(Locale.ROOT);
        if (command.senha().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessValidationException("A senha excede 72 bytes.");
        }
        if (repository.buscarIdPorEmail(email).filter(outroId -> !id.equals(outroId)).isPresent()) {
            throw new EmailAlreadyExistsException();
        }
        User atualizado = User.reconstituir(id, command.nome().strip(), email,
                hasher.hash(command.senha()), atual.getRole(), atual.getCreatedAt(), Instant.now(clock));
        if (!repository.atualizar(atualizado)) {
            throw new UserNotFoundException();
        }
        return atualizado;
    }

    @Override
    public void excluir(UUID id) {
        buscar(id);
        if (!repository.excluir(id)) {
            throw new UserNotFoundException();
        }
    }
}
