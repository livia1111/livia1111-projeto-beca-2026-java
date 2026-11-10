package com.nttdata.users.adapter.in.security;

import com.nttdata.users.application.port.in.AuthenticateUserUseCase;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public final class BasicAuthenticationProvider implements AuthenticationProvider {
    private final AuthenticateUserUseCase authenticate;

    public BasicAuthenticationProvider(AuthenticateUserUseCase authenticate) {
        this.authenticate = authenticate;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        try {
            var user = authenticate.autenticar(authentication.getName(),
                    authentication.getCredentials() == null ? null : authentication.getCredentials().toString());
            var principal = new AuthenticatedUserDetails(user.getId(), user.getEmail(), user.getSenha(), user.getRole());
            return UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
        } catch (com.nttdata.users.application.exception.InvalidCredentialsException ex) {
            throw new BadCredentialsException("E-mail ou senha incorretos.", ex);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
