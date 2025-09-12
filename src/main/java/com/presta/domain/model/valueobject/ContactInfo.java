package com.presta.domain.model.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

public record ContactInfo(String email) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    public ContactInfo {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        String trimmedEmail = email.trim().toLowerCase();

        if (trimmedEmail.length() > 100) {
            throw new IllegalArgumentException("Email cannot exceed 100 characters");
        }

        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }

        email = trimmedEmail; // réaffectation au champ du record
    }

    public static ContactInfo of(String email) {
        return new ContactInfo(email);
    }

    public String getValue() {
        return email;
    }
}
