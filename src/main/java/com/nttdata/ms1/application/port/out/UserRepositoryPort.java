package com.nttdata.ms1.application.port.out;

import com.nttdata.ms1.domain.model.User;
import com.nttdata.ms1.domain.model.UserEmail;
import com.nttdata.ms1.domain.model.UserId;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    boolean existsByEmail(UserEmail email);

    Optional<User> findById(UserId id);

    List<User> findAll();

    void deleteById(UserId id);
}
