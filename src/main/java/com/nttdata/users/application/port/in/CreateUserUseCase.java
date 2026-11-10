package com.nttdata.users.application.port.in;

import com.nttdata.users.domain.model.User;

public interface CreateUserUseCase {

    User criar(CreateUserCommand command);
}
