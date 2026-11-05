package com.nttdata.ms1.application.service;

import com.nttdata.ms1.application.port.in.DeleteUserUseCase;
import com.nttdata.ms1.application.port.out.UserRepositoryPort;
import com.nttdata.ms1.domain.exception.UserNotFoundException;
import com.nttdata.ms1.domain.model.UserId;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteUserService implements DeleteUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public DeleteUserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public void execute(UUID id) {
        UserId userId = new UserId(id);

        if (userRepositoryPort.findById(userId).isEmpty()) {
            throw new UserNotFoundException("user not found: " + id);
        }

        userRepositoryPort.deleteById(userId);
    }
}
