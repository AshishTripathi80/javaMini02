package com.javamini02.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends RuntimeException {
    private final String message;
    private final int code;

    public ValidationException(String message, HttpStatus status) {
        this.message = message;
        this.code = status.value();
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }
}

