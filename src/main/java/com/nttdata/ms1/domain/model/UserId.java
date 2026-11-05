package com.nttdata.ms1.domain.model;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID value) {

    public UserId {
        Objects.requireNonNull(value, "user id must not be null");
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID());
    }
}
