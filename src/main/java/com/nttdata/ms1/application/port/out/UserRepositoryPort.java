package com.nttdata.ms1.application.port.out;

import com.nttdata.ms1.domain.model.User;
import com.nttdata.ms1.domain.model.UserEmail;

public interface UserRepositoryPort {

    User save(User user);

    boolean existsByEmail(UserEmail email);
}
