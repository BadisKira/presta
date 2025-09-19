package com.presta.domain.model;

import java.util.Objects;
import java.util.UUID;

public record Client(UUID id, User user) {
    public Client {
        Objects.requireNonNull(id, "Client ID cannot be null");
        Objects.requireNonNull(user, "Client must have a User");
    }

    public static Client create(UUID id, User user) {
        return new Client(id, user);
    }
}