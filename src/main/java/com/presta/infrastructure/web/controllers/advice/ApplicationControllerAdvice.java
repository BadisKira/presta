package com.presta.infrastructure.web.controllers.advice;


import com.presta.domain.exception.DomainException;
import com.presta.infrastructure.web.dtos.ExceptionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApplicationControllerAdvice {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ExceptionDTO> handleDomainException(DomainException ex) {
        ExceptionDTO body = new ExceptionDTO(ex);
        return ResponseEntity
                .status(HttpStatus.valueOf(ex.getStatusCode()))
                .body(body);
    }
}
