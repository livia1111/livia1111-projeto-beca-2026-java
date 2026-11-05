package com.nttdata.ms1.application.service;

import com.nttdata.ms1.application.port.out.UserRepositoryPort;
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
class DeleteUserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private DeleteUserService deleteUserService;

    @Test
    void shouldDeleteUserWhenUserExists() {
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

        when(userRepositoryPort.findById(ArgumentMatchers.any(UserId.class))).thenReturn(Optional.of(existingUser));

        // Act + Assert
        assertDoesNotThrow(() -> deleteUserService.execute(id));
        verify(userRepositoryPort).deleteById(ArgumentMatchers.any(UserId.class));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(userRepositoryPort.findById(ArgumentMatchers.any(UserId.class))).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(UserNotFoundException.class, () -> deleteUserService.execute(id));
    }
}
