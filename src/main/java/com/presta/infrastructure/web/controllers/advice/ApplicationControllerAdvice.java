package com.presta.infrastructure.web.controllers.advice;

import com.presta.domain.exception.DomainException;
import com.presta.infrastructure.web.dtos.ExceptionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApplicationControllerAdvice {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ExceptionDTO> handleDomainException(DomainException ex) {
        ExceptionDTO body = new ExceptionDTO(ex);
        return ResponseEntity
                .status(HttpStatus.valueOf(ex.getStatusCode()))
                .body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        ExceptionDTO errorResponse = new ExceptionDTO(
                "INVALID_ARGUMENT",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.name(),
                HttpStatus.BAD_REQUEST.value(),
                Instant.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ExceptionDTO> handleIllegalStateException(IllegalStateException ex) {
        ExceptionDTO errorResponse = new ExceptionDTO(
                "ILLEGAL_STATE",
                ex.getMessage(),
                HttpStatus.CONFLICT.name(),
                HttpStatus.CONFLICT.value(),
                Instant.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ExceptionDTO> handleNullPointerException(NullPointerException ex) {
        ExceptionDTO errorResponse = new ExceptionDTO(
                "INTERNAL_ERROR",
                "Une erreur interne est survenue",
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                Instant.now()
        );

        // Log l'exception pour le debugging (ne pas exposer les détails au client)
        ex.printStackTrace();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Gère les erreurs de validation (@Valid sur les DTOs)
     * Code HTTP : 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("code", "VALIDATION_ERROR");
        response.put("message", "Erreur de validation");
        response.put("status", HttpStatus.BAD_REQUEST.name());
        response.put("statusCode", HttpStatus.BAD_REQUEST.value());
        response.put("timestamp", Instant.now());
        response.put("errors", fieldErrors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ExceptionDTO> handleUnsupportedOperationException(
            UnsupportedOperationException ex) {

        ExceptionDTO errorResponse = new ExceptionDTO(
                "NOT_IMPLEMENTED",
                ex.getMessage(),
                HttpStatus.NOT_IMPLEMENTED.name(),
                HttpStatus.NOT_IMPLEMENTED.value(),
                Instant.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * Gère les SecurityException
     * Code HTTP : 403 Forbidden
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ExceptionDTO> handleSecurityException(SecurityException ex) {
        ExceptionDTO errorResponse = new ExceptionDTO(
                "ACCESS_DENIED",
                "Accès refusé",
                HttpStatus.FORBIDDEN.name(),
                HttpStatus.FORBIDDEN.value(),
                Instant.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handler par défaut pour toutes les autres exceptions non gérées
     * Code HTTP : 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDTO> handleGlobalException(Exception ex) {
        ExceptionDTO errorResponse = new ExceptionDTO(
                "UNEXPECTED_ERROR",
                "Une erreur inattendue est survenue",
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                Instant.now()
        );

        // Log l'exception complète pour le debugging
        System.err.println("Exception non gérée : " + ex.getClass().getName());
        ex.printStackTrace();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}