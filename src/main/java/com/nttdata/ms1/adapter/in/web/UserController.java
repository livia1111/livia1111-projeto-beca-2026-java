package com.nttdata.ms1.adapter.in.web;

import com.nttdata.ms1.adapter.in.web.dto.CreateUserRequest;
import com.nttdata.ms1.adapter.in.web.dto.UserResponse;
import com.nttdata.ms1.application.port.in.CreateUserUseCase;
import com.nttdata.ms1.domain.exception.DuplicateEmailException;
import com.nttdata.ms1.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;

    public UserController(CreateUserUseCase createUserUseCase) {
        this.createUserUseCase = createUserUseCase;
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

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<String> handleDuplicateEmail(DuplicateEmailException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
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
