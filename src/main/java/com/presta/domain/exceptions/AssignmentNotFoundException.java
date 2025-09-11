package com.presta.domain.exceptions;


import java.util.UUID;

public class AssignmentNotFoundException extends RuntimeException {
    public AssignmentNotFoundException(UUID id) {
        super("Service not found: " + id);
    }
}
