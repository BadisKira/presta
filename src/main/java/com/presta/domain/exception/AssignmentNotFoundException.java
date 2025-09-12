package com.presta.domain.exception;


import java.util.UUID;

public class AssignmentNotFoundException extends RuntimeException {
    public AssignmentNotFoundException(UUID id) {
        super("Service not found: " + id);
    }
}
