package com.nttdata.ms1.application.service;

import com.nttdata.ms1.application.port.in.CreateUserUseCase;
import com.nttdata.ms1.application.port.out.UserRepositoryPort;
import com.nttdata.ms1.domain.exception.DuplicateEmailException;
import com.nttdata.ms1.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private CreateUserService createUserService;

    @Test
    void shouldCreateUserWhenEmailDoesNotExist() {
        // Arrange
        CreateUserUseCase.Command command = new CreateUserUseCase.Command(
                "Ana Silva",
                "ana@email.com",
                "123456"
        );

        when(userRepositoryPort.existsByEmail(ArgumentMatchers.any())).thenReturn(false);
        when(userRepositoryPort.save(ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        createUserService.execute(command);

        // Assert
        verify(userRepositoryPort).existsByEmail(ArgumentMatchers.any());
        verify(userRepositoryPort).save(ArgumentMatchers.any(User.class));
    }

    @Test
    void shouldThrowDuplicateEmailExceptionWhenEmailAlreadyExists() {
        // Arrange
        CreateUserUseCase.Command command = new CreateUserUseCase.Command(
                "Ana Silva",
                "ana@email.com",
                "123456"
        );

        when(userRepositoryPort.existsByEmail(ArgumentMatchers.any())).thenReturn(true);

        // Act + Assert
        assertThrows(DuplicateEmailException.class, () -> createUserService.execute(command));
    }
}
