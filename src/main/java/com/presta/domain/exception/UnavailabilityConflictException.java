package com.presta.domain.exception;

public class UnavailabilityConflictException extends DomainException{
    public UnavailabilityConflictException(String message, String code, int statusCode) {
        super(message, code, statusCode);
    }
}
