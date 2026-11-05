package com.nttdata.ms1.domain.model;

import java.util.regex.Pattern;

public record UserEmail(String value) {

    private static final Pattern SIMPLE_EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public UserEmail {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("user email must not be null or blank");
        }

        String normalized = value.trim().toLowerCase();
        if (normalized.length() > 254) {
            throw new IllegalArgumentException("user email must not exceed 254 characters");
        }

        if (!SIMPLE_EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("user email format is invalid");
        }

        value = normalized;
    }
}
