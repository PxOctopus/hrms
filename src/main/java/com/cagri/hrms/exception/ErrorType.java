package com.cagri.hrms.exception;

import org.springframework.http.HttpStatus;

public enum ErrorType {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed"),
    AUTHENTICATION_ERROR(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_ERROR", "Authentication failed"),
    AUTHORIZATION_ERROR(HttpStatus.FORBIDDEN, "AUTHORIZATION_ERROR", "Access denied"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "Resource not found"),
    BUSINESS_ERROR(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_ERROR", "Business rule violation"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected server error");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorType(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

