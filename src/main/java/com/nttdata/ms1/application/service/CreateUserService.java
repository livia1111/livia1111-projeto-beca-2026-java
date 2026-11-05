package com.nttdata.ms1.application.service;

import com.nttdata.ms1.application.port.in.CreateUserUseCase;
import com.nttdata.ms1.application.port.out.UserRepositoryPort;
import com.nttdata.ms1.domain.exception.DuplicateEmailException;
import com.nttdata.ms1.domain.model.User;
import com.nttdata.ms1.domain.model.UserEmail;
import com.nttdata.ms1.domain.model.UserName;
import com.nttdata.ms1.domain.model.UserPassword;
import org.springframework.stereotype.Service;

@Service
public class CreateUserService implements CreateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public CreateUserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public User execute(Command command) {
        UserEmail email = new UserEmail(command.email());

        if (userRepositoryPort.existsByEmail(email)) {
            throw new DuplicateEmailException("email already registered: " + email.value());
        }

        User user = User.createNew(
                new UserName(command.name()),
                email,
                new UserPassword(command.password())
        );

        return userRepositoryPort.save(user);
    }
}
