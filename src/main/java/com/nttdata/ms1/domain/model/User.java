package com.nttdata.ms1.domain.model;

import java.time.Instant;
import java.util.Objects;

public final class User {

    private final UserId id;
    private UserName name;
    private UserEmail email;
    private UserPassword password;
    private final Instant createdAt;
    private Instant updatedAt;

    private User(
            UserId id,
            UserName name,
            UserEmail email,
            UserPassword password,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "user id must not be null");
        this.name = Objects.requireNonNull(name, "user name must not be null");
        this.email = Objects.requireNonNull(email, "user email must not be null");
        this.password = Objects.requireNonNull(password, "user password must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");

        if (this.updatedAt.isBefore(this.createdAt)) {
            throw new IllegalArgumentException("updatedAt must not be before createdAt");
        }
    }

    public static User createNew(
            UserName name,
            UserEmail email,
            UserPassword password
    ) {
        Instant now = Instant.now();
        return new User(
                UserId.newId(),
                name,
                email,
                password,
                now,
                now
        );
    }

    public static User reconstitute(
            UserId id,
            UserName name,
            UserEmail email,
            UserPassword password,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new User(id, name, email, password, createdAt, updatedAt);
    }

    public void changeName(UserName newName) {
        this.name = Objects.requireNonNull(newName, "user name must not be null");
        touch();
    }

    public void changeEmail(UserEmail newEmail) {
        this.email = Objects.requireNonNull(newEmail, "user email must not be null");
        touch();
    }

    public void changePassword(UserPassword newPassword) {
        this.password = Objects.requireNonNull(newPassword, "user password must not be null");
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public UserId id() {
        return id;
    }

    public UserName name() {
        return name;
    }

    public UserEmail email() {
        return email;
    }

    public UserPassword password() {
        return password;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
