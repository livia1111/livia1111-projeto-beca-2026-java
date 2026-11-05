package com.nttdata.ms1.domain.model;

public record UserName(String value) {

    public UserName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("user name must not be null or blank");
        }

        String normalized = value.trim();
        if (normalized.length() < 3 || normalized.length() > 120) {
            throw new IllegalArgumentException("user name length must be between 3 and 120 characters");
        }

        value = normalized;
    }
}
