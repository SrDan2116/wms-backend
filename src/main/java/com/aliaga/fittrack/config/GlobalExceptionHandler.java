package com.aliaga.fittrack.config;

import com.aliaga.fittrack.exception.UserSuspendedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserSuspendedException.class)
    public ResponseEntity<Map<String, String>> handleSuspension(UserSuspendedException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "ACCOUNT_SUSPENDED");
        response.put("reason", ex.getReason());
        response.put("endsAt", ex.getEndsAt() != null ? ex.getEndsAt().toString() : "Indefinido");
        
        // Retornamos 403 Forbidden
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}