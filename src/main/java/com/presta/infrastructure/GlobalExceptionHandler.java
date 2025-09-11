package com.presta.infrastructure;

import com.presta.domain.exceptions.AssignmentNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AssignmentNotFoundException.class)
    public ResponseEntity<String> handleNotFound(AssignmentNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleOther(Exception ex) {
        // For dev: return message; in prod, return generic message and log actual error
        return ResponseEntity.status(500).body("Internal server error: " + ex.getMessage());
    }
}
