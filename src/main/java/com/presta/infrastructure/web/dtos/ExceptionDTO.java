package com.presta.infrastructure.web.dtos;

import com.presta.domain.exception.DomainException;
import org.springframework.http.HttpStatus;

import java.time.Instant;

public record ExceptionDTO(
        String code,
        String message,
        String status,
        int statusCode,
        Instant timestamp
) {
    public ExceptionDTO(DomainException ex) {
        this(
                ex.getCode(),
                ex.getMessage(),
                HttpStatus.valueOf(ex.getStatusCode()).name(),
                ex.getStatusCode(),
                Instant.now()
        );
    }
}