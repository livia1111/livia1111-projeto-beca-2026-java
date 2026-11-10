package com.nttdata.users.configuration;

import com.nttdata.users.adapter.in.security.BasicAuthenticationProvider;
import com.nttdata.users.adapter.in.web.error.SecurityErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityErrorHandler errorHandler,
                                                    BasicAuthenticationProvider provider) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(provider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/status", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                        // Permite que o MS2 consulte a existência de um usuário sem Basic Auth
                        .requestMatchers(HttpMethod.GET, "/users/*/exists").permitAll() 
                        
                        .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/users", "/users/import").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler))
                .httpBasic(basic -> basic.authenticationEntryPoint(errorHandler))
                .build();
    }

    @Bean
    public SecurityErrorHandler securityErrorHandler() {
        return new SecurityErrorHandler();
    }

    @Bean
    public BasicAuthenticationProvider basicAuthenticationProvider(
            com.nttdata.users.application.port.in.AuthenticateUserUseCase authenticateUser) {
        return new BasicAuthenticationProvider(authenticateUser);
    }
}