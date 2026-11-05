package com.nttdata.ms1.adapter.in.web;

import com.nttdata.ms1.adapter.in.web.dto.CreateUserRequest;
import com.nttdata.ms1.adapter.in.web.dto.UpdateUserRequest;
import com.nttdata.ms1.adapter.in.web.dto.UserResponse;
import com.nttdata.ms1.application.port.in.CreateUserUseCase;
import com.nttdata.ms1.application.port.in.GetUserByIdUseCase;
import com.nttdata.ms1.application.port.in.ListUsersUseCase;
import com.nttdata.ms1.application.port.in.UpdateUserUseCase;
import com.nttdata.ms1.domain.exception.DuplicateEmailException;
import com.nttdata.ms1.domain.exception.UserNotFoundException;
import com.nttdata.ms1.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final UpdateUserUseCase updateUserUseCase;

    public UserController(
            CreateUserUseCase createUserUseCase,
            GetUserByIdUseCase getUserByIdUseCase,
            ListUsersUseCase listUsersUseCase,
            UpdateUserUseCase updateUserUseCase
    ) {
        this.createUserUseCase = createUserUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.listUsersUseCase = listUsersUseCase;
        this.updateUserUseCase = updateUserUseCase;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        User createdUser = createUserUseCase.execute(
                new CreateUserUseCase.Command(
                        request.name(),
                        request.email(),
                        request.password()
                )
        );

        UserResponse response = toResponse(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        User user = getUserByIdUseCase.execute(id);
        return ResponseEntity.ok(toResponse(user));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> list() {
        List<UserResponse> users = listUsersUseCase.execute().stream().map(this::toResponse).toList();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        User updatedUser = updateUserUseCase.execute(
                id,
                new UpdateUserUseCase.Command(
                        request.name(),
                        request.email(),
                        request.password()
                )
        );
        return ResponseEntity.ok(toResponse(updatedUser));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<String> handleDuplicateEmail(DuplicateEmailException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.id().value(),
                user.name().value(),
                user.email().value(),
                user.createdAt(),
                user.updatedAt()
        );
    }
}
