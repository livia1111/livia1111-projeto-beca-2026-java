package com.nttdata.users.adapter.out.security;

import com.nttdata.users.application.port.out.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class BCryptPasswordHasherAdapter implements PasswordHasher {

    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordHasherAdapter(BCryptPasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return encoder.matches(rawPassword, passwordHash);
    }
}
