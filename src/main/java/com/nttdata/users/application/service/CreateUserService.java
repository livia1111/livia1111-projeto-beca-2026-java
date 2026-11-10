package com.nttdata.users.application.service;

import com.nttdata.users.application.exception.BusinessValidationException;
import com.nttdata.users.application.exception.EmailAlreadyExistsException;
import com.nttdata.users.application.port.in.CreateUserCommand;
import com.nttdata.users.application.port.in.CreateUserUseCase;
import com.nttdata.users.application.port.out.PasswordHasher;
import com.nttdata.users.application.port.out.UserRepository;
import com.nttdata.users.domain.model.User;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Locale;

public class CreateUserService implements CreateUserUseCase {
    private final UserRepository repository;
    private final PasswordHasher hasher;
    private final Clock clock;

    public CreateUserService(UserRepository repository, PasswordHasher hasher, Clock clock) {
        this.repository = repository;
        this.hasher = hasher;
        this.clock = clock;
    }

    @Override
    public User criar(CreateUserCommand command) {
        String email = command.email().strip().toLowerCase(Locale.ROOT);
        if (command.senha().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessValidationException("A senha excede 72 bytes.");
        }
        if (repository.buscarEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException();
        }
        User user = User.criar(command.nome().strip(), email,
                hasher.hash(command.senha()), command.role(), clock);
        return repository.salvar(user);
    }
}
