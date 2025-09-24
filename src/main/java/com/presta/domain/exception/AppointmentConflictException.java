package com.presta.domain.exception;

public class AppointmentConflictException extends DomainException{
    public AppointmentConflictException(String message, String code, int statusCode) {
        super(message, code, statusCode);
    }
}
