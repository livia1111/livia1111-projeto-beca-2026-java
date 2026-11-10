package com.nttdata.users.configuration;

import com.nttdata.users.adapter.out.security.BCryptPasswordHasherAdapter;
import com.nttdata.users.application.port.out.PasswordHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration(proxyBeanMethods = false)
public class PasswordConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public PasswordHasher passwordHasher(PasswordEncoder encoder) {
        return new BCryptPasswordHasherAdapter((BCryptPasswordEncoder) encoder);
    }
}
