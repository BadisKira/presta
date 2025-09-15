package com.presta.domain.exception;


import java.util.UUID;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(UUID keycloakId) {
        super("User not found: " + keycloakId, "USER_NOT_FOUND", 404);
    }
}