package com.nttdata.ms1.application.service;

import com.nttdata.ms1.application.port.out.UserRepositoryPort;
import com.nttdata.ms1.domain.model.User;
import com.nttdata.ms1.domain.model.UserEmail;
import com.nttdata.ms1.domain.model.UserId;
import com.nttdata.ms1.domain.model.UserName;
import com.nttdata.ms1.domain.model.UserPassword;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListUsersServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private ListUsersService listUsersService;

    @Test
    void shouldReturnAllUsersFromRepository() {
        // Arrange
        User user1 = User.reconstitute(
                new UserId(UUID.randomUUID()),
                new UserName("Ana Silva"),
                new UserEmail("ana@email.com"),
                new UserPassword("123456"),
                Instant.now(),
                Instant.now()
        );

        User user2 = User.reconstitute(
                new UserId(UUID.randomUUID()),
                new UserName("Bruno Costa"),
                new UserEmail("bruno@email.com"),
                new UserPassword("abcdef"),
                Instant.now(),
                Instant.now()
        );

        when(userRepositoryPort.findAll()).thenReturn(List.of(user1, user2));

        // Act
        List<User> users = listUsersService.execute();

        // Assert
        assertEquals(2, users.size());
        assertEquals("ana@email.com", users.get(0).email().value());
        assertEquals("bruno@email.com", users.get(1).email().value());
    }
}
