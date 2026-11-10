package com.nttdata.users.application.port.in;

import com.nttdata.users.domain.model.User;

public interface AuthenticateUserUseCase {
    User autenticar(String email, String senha);
}
