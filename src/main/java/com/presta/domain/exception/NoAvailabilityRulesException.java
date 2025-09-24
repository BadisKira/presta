package com.presta.domain.exception;

public class NoAvailabilityRulesException extends DomainException{
    public NoAvailabilityRulesException(String message, String code, int statusCode) {
        super(message, code, statusCode);
    }
}
