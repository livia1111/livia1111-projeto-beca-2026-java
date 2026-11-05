package com.nttdata.ms1.application.service;

import com.nttdata.ms1.application.port.in.UpdateUserUseCase;
import com.nttdata.ms1.application.port.out.UserRepositoryPort;
import com.nttdata.ms1.domain.exception.DuplicateEmailException;
import com.nttdata.ms1.domain.exception.UserNotFoundException;
import com.nttdata.ms1.domain.model.User;
import com.nttdata.ms1.domain.model.UserEmail;
import com.nttdata.ms1.domain.model.UserId;
import com.nttdata.ms1.domain.model.UserName;
import com.nttdata.ms1.domain.model.UserPassword;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateUserService implements UpdateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public UpdateUserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public User execute(UUID id, Command command) {
        User user = userRepositoryPort.findById(new UserId(id))
                .orElseThrow(() -> new UserNotFoundException("user not found: " + id));

        UserEmail newEmail = new UserEmail(command.email());
        if (!user.email().value().equals(newEmail.value()) && userRepositoryPort.existsByEmail(newEmail)) {
            throw new DuplicateEmailException("email already registered: " + newEmail.value());
        }

        user.changeName(new UserName(command.name()));
        user.changeEmail(newEmail);
        user.changePassword(new UserPassword(command.password()));

        return userRepositoryPort.save(user);
    }
}
