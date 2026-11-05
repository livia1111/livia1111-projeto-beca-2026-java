package com.nttdata.ms1.adapter.out.persistence;

import com.nttdata.ms1.application.port.out.UserRepositoryPort;
import com.nttdata.ms1.domain.model.User;
import com.nttdata.ms1.domain.model.UserEmail;
import com.nttdata.ms1.domain.model.UserId;
import com.nttdata.ms1.domain.model.UserName;
import com.nttdata.ms1.domain.model.UserPassword;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;

    public UserPersistenceAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toEntity(user);
        UserJpaEntity savedEntity = repository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public boolean existsByEmail(UserEmail email) {
        return repository.existsByEmail(email.value());
    }

    private UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.id().value(),
                user.name().value(),
                user.email().value(),
                user.password().value(),
                user.createdAt(),
                user.updatedAt()
        );
    }

    private User toDomain(UserJpaEntity entity) {
        return User.reconstitute(
                new UserId(entity.getId()),
                new UserName(entity.getName()),
                new UserEmail(entity.getEmail()),
                new UserPassword(entity.getPassword()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
