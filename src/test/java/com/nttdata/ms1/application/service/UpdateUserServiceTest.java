package com.nttdata.ms1.application.service;

import com.nttdata.ms1.application.port.in.UpdateUserUseCase;
import com.nttdata.ms1.application.port.out.UserRepositoryPort;
import com.nttdata.ms1.domain.exception.DuplicateEmailException;
import com.nttdata.ms1.domain.exception.UserNotFoundException;
import com.nttdata.ms1.domain.model.User;
import com.nttdata.ms1.domain.model.UserEmail;
import com.nttdata.ms1.domain.model.UserId;
import com.nttdata.ms1.domain.model.UserName;
import com.nttdata.ms1.domain.model.UserPassword;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private UpdateUserService updateUserService;

    @Test
    void shouldUpdateUserWhenUserExistsAndEmailIsAvailable() {
        // Arrange
        UUID id = UUID.randomUUID();
        User existingUser = User.reconstitute(
                new UserId(id),
                new UserName("Ana Silva"),
                new UserEmail("ana@email.com"),
                new UserPassword("123456"),
                Instant.now(),
                Instant.now()
        );

        UpdateUserUseCase.Command command = new UpdateUserUseCase.Command(
                "Ana Clara",
                "ana.clara@email.com",
                "654321"
        );

        when(userRepositoryPort.findById(ArgumentMatchers.any(UserId.class))).thenReturn(Optional.of(existingUser));
        when(userRepositoryPort.existsByEmail(ArgumentMatchers.any(UserEmail.class))).thenReturn(false);
        when(userRepositoryPort.save(ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act + Assert
        assertDoesNotThrow(() -> updateUserService.execute(id, command));
        verify(userRepositoryPort).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        // Arrange
        UUID id = UUID.randomUUID();
        UpdateUserUseCase.Command command = new UpdateUserUseCase.Command(
                "Ana Clara",
                "ana.clara@email.com",
                "654321"
        );
        when(userRepositoryPort.findById(ArgumentMatchers.any(UserId.class))).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(UserNotFoundException.class, () -> updateUserService.execute(id, command));
    }

    @Test
    void shouldThrowDuplicateEmailExceptionWhenNewEmailAlreadyExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        User existingUser = User.reconstitute(
                new UserId(id),
                new UserName("Ana Silva"),
                new UserEmail("ana@email.com"),
                new UserPassword("123456"),
                Instant.now(),
                Instant.now()
        );

        UpdateUserUseCase.Command command = new UpdateUserUseCase.Command(
                "Ana Clara",
                "email.duplicado@email.com",
                "654321"
        );

        when(userRepositoryPort.findById(ArgumentMatchers.any(UserId.class))).thenReturn(Optional.of(existingUser));
        when(userRepositoryPort.existsByEmail(ArgumentMatchers.any(UserEmail.class))).thenReturn(true);

        // Act + Assert
        assertThrows(DuplicateEmailException.class, () -> updateUserService.execute(id, command));
    }
}
