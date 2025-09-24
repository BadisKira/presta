package com.presta.domain.exception;

public class OutsideAvailabilityException extends DomainException{
    public OutsideAvailabilityException(String message, String code, int statusCode) {
        super(message, code, statusCode);
    }
}
