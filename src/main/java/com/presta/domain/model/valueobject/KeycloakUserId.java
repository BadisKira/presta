package com.presta.domain.model.valueobject;

import java.util.Objects;
import java.util.UUID;

public class KeycloakUserId {
    private final UUID value;

    private KeycloakUserId(UUID value) {
        this.value = Objects.requireNonNull(value, "KeycloakUserId cannot be null");
    }

    public static KeycloakUserId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("KeycloakUserId cannot be null or empty");
        }
        try {
            return new KeycloakUserId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid KeycloakUserId format: " + value);
        }
    }

    public static KeycloakUserId of(UUID value) {
        return new KeycloakUserId(value);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeycloakUserId that = (KeycloakUserId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}