package com.nttdata.ms1.application.port.in;

import com.nttdata.ms1.domain.model.User;

public interface CreateUserUseCase {

    User execute(Command command);

    record Command(
            String name,
            String email,
            String password
    ) {
    }
}
