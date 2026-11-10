package com.nttdata.users.adapter.in.web.mapper;

import com.nttdata.users.adapter.in.web.dto.CreateUserRequest;
import com.nttdata.users.adapter.in.web.dto.UserResponse;
import com.nttdata.users.application.port.in.CreateUserCommand;
import com.nttdata.users.application.port.in.UpdateUserCommand;
import com.nttdata.users.adapter.in.web.dto.UpdateUserRequest;
import com.nttdata.users.domain.model.User;

public final class UserWebMapper {

    public UpdateUserCommand toCommand(UpdateUserRequest request) {
        return new UpdateUserCommand(request.nome(), request.email(), request.senha());
    }

    public CreateUserCommand toCommand(CreateUserRequest request) {
        return new CreateUserCommand(
                request.nome(), request.email(), request.senha(), request.role()
        );
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(), user.getNome(), user.getEmail(), user.getRole(),
                user.getCreatedAt(), user.getUpdatedAt()
        );
    }
}
