package com.nttdata.ms1.application.service;

import com.nttdata.ms1.application.port.in.GetUserByIdUseCase;
import com.nttdata.ms1.application.port.out.UserRepositoryPort;
import com.nttdata.ms1.domain.exception.UserNotFoundException;
import com.nttdata.ms1.domain.model.User;
import com.nttdata.ms1.domain.model.UserId;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetUserByIdService implements GetUserByIdUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public GetUserByIdService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public User execute(UUID id) {
        return userRepositoryPort
                .findById(new UserId(id))
                .orElseThrow(() -> new UserNotFoundException("user not found: " + id));
    }
}
