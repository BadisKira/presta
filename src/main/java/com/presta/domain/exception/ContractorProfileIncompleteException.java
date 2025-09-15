package com.presta.domain.exception;

public class ContractorProfileIncompleteException extends DomainException {
    public ContractorProfileIncompleteException() {
        super("Contractor profile is incomplete", "CONTRACTOR_PROFILE_INCOMPLETE", 400);
    }
}