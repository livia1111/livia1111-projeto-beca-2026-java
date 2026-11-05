package com.nttdata.ms1.application.port.in;

import com.nttdata.ms1.domain.model.User;

import java.util.List;

public interface ListUsersUseCase {

    List<User> execute();
}
