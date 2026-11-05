package com.nttdata.ms1.application.port.in;

import com.nttdata.ms1.domain.model.User;

import java.util.UUID;

public interface UpdateUserUseCase {

    User execute(UUID id, Command command);

    record Command(
            String name,
            String email,
            String password
    ) {
    }
}
