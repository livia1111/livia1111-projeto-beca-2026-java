package com.nttdata.ms1.domain.model;

public record UserPassword(String value) {

    public UserPassword {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("user password must not be null or blank");
        }

        String normalized = value.trim();
        if (normalized.length() < 6 || normalized.length() > 120) {
            throw new IllegalArgumentException("user password length must be between 6 and 120 characters");
        }

        value = normalized;
    }
}
