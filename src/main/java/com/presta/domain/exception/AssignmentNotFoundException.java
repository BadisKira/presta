package com.presta.domain.exception;


import java.util.UUID;

public class AssignmentNotFoundException extends DomainException {
    public AssignmentNotFoundException(UUID id) {
        super("Assignment not found: " + id, "ASSIGNMENT_NOT_FOUND", 404);
    }
}