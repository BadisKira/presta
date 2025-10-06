package com.presta.domain.exception;

public class SlotNotAvailableException extends DomainException {
    public SlotNotAvailableException(String message) {
        super(message,"SLOT NOT AVAILABLE",400);
    }
}