package com.presta.domain.exception;

import java.util.UUID;

public class UserAlreadyExistsException extends DomainException {
    public UserAlreadyExistsException(UUID keycloakId) {
        super("User already exists with Keycloak ID: " + keycloakId,
                "USER_ALREADY_EXISTS",
                409);
    }
}