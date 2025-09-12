package com.presta.domain.model;

import java.util.Objects;
import java.util.UUID;

public record Assignment(UUID id, String name, String description) {

    public Assignment {
        Objects.requireNonNull(id, "Assignment ID cannot be null");
        validateName(name);
    }

    public static Assignment create(String name, String description) {
        return new Assignment(UUID.randomUUID(), name, description);
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Assignment name cannot be null or empty");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Assignment name cannot exceed 100 characters");
        }
    }

    public Assignment updateName(String newName) {
        validateName(newName);
        return new Assignment(id, newName, description);
    }

    public Assignment updateDescription(String newDescription) {
        return new Assignment(id, name, newDescription);
    }
}