package com.nttdata.users.configuration;

import com.nttdata.users.adapter.out.persistence.UserJpaRepository;
import com.nttdata.users.adapter.out.persistence.UserPersistenceAdapter;
import com.nttdata.users.adapter.out.persistence.UserPersistenceExceptionTranslator;
import com.nttdata.users.application.port.in.CreateUserUseCase;
import com.nttdata.users.application.port.in.AuthenticateUserUseCase;
import com.nttdata.users.application.port.in.ManageUserUseCase;
import com.nttdata.users.application.port.out.PasswordHasher;
import com.nttdata.users.application.port.out.UserRepository;
import com.nttdata.users.application.service.CreateUserService;
import com.nttdata.users.application.service.AuthenticateUserService;
import com.nttdata.users.application.service.ManageUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import java.time.Clock;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(basePackageClasses = UserJpaRepository.class)
public class UserConfiguration {
    @Bean
    public Clock clock() { return Clock.systemUTC(); }

    @Bean
    public UserPersistenceExceptionTranslator userPersistenceExceptionTranslator() {
        return new UserPersistenceExceptionTranslator();
    }

    @Bean
    public UserRepository userRepository(UserJpaRepository jpa, UserPersistenceExceptionTranslator translator,
                                         PlatformTransactionManager transactionManager) {
        return new UserPersistenceAdapter(jpa, translator, new TransactionTemplate(transactionManager));
    }

    @Bean
    public CreateUserUseCase createUserUseCase(UserRepository repository, PasswordHasher hasher, Clock clock) {
        return new CreateUserService(repository, hasher, clock);
    }

    @Bean
    public AuthenticateUserUseCase authenticateUserUseCase(UserRepository repository, PasswordHasher hasher) {
        return new AuthenticateUserService(repository, hasher);
    }

    @Bean
    public ManageUserUseCase manageUserUseCase(UserRepository repository, PasswordHasher hasher, Clock clock) {
        return new ManageUserService(repository, hasher, clock);
    }
}
