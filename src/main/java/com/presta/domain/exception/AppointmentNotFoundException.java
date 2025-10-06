package com.presta.domain.exception;

public class AppointmentNotFoundException extends DomainException {
    public AppointmentNotFoundException(String message) {
        super(message,"APPOINTMENT NOT FOUND",404);
    }
}