package com.presta.domain.exception;

public abstract class DomainException extends RuntimeException {
    private final String code;
    private final int statusCode;

    protected DomainException(String message, String code, int statusCode) {
        super(message);
        this.code = code;
        this.statusCode = statusCode;
    }

    public String getCode() {
        return code;
    }

    public int getStatusCode() {
        return statusCode;
    }
}