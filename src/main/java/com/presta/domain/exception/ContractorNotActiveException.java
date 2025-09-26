package com.presta.domain.exception;


public class ContractorNotActiveException extends DomainException {
    public ContractorNotActiveException(String message) {
        super(message,"CONTRACTOR NOT ACTIVE",400);
    }
}