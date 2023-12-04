package com.javamini02.handler;

import com.javamini02.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("code", ex.getCode());
        // Format the timestamp
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd'-'MMM yyyy HH:mm:ss");
        String formattedTimestamp = LocalDateTime.now().format(formatter);
        errorResponse.put("timestamp", formattedTimestamp);

        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(ex.getCode()));
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", "An unexpected error occurred");
        errorResponse.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        // Format the timestamp
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd'-'MMM yyyy HH:mm:ss");
        String formattedTimestamp = LocalDateTime.now().format(formatter);
        errorResponse.put("timestamp", formattedTimestamp);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("code", HttpStatus.BAD_REQUEST.value());
        // Format the timestamp
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd'-'MMM yyyy HH:mm:ss");
        String formattedTimestamp = LocalDateTime.now().format(formatter);
        errorResponse.put("timestamp", formattedTimestamp);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
