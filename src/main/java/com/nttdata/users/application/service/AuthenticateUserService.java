package com.nttdata.users.application.service;

import com.nttdata.users.application.exception.InvalidCredentialsException;
import com.nttdata.users.application.port.in.AuthenticateUserUseCase;
import com.nttdata.users.application.port.out.PasswordHasher;
import com.nttdata.users.application.port.out.UserRepository;
import com.nttdata.users.domain.model.User;

import java.util.Locale;

public class AuthenticateUserService implements AuthenticateUserUseCase {
    private final UserRepository repository;
    private final PasswordHasher hasher;

    public AuthenticateUserService(UserRepository repository, PasswordHasher hasher) {
        this.repository = repository;
        this.hasher = hasher;
    }

    @Override
    public User autenticar(String email, String senha) {
        if (email == null || email.isBlank() || senha == null) {
            throw new InvalidCredentialsException();
        }
        String normalizado = email.strip().toLowerCase(Locale.ROOT);
        User user = repository.buscarPorEmailCompleto(normalizado)
                .orElseThrow(InvalidCredentialsException::new);
        if (!hasher.matches(senha, user.getSenha())) {
            throw new InvalidCredentialsException();
        }
        return user;
    }
}
