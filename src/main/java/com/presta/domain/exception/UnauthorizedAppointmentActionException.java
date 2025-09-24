package com.presta.domain.exception;

public class UnauthorizedAppointmentActionException extends DomainException {
    public UnauthorizedAppointmentActionException(String message) {
        super(message,"NOT AUTHORIZED TO DO THIS",403);
    }
}
