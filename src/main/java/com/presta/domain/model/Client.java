package com.presta.domain.model;

import java.util.Objects;
import java.util.UUID;


public record Client(UUID id) {

    public Client {
        Objects.requireNonNull(id, "Client ID cannot be null");
    }

    public static Client create(UUID userId) {
        return new Client(userId);
    }
}