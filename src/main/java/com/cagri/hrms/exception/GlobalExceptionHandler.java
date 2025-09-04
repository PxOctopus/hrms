package com.cagri.hrms.exception;

import com.cagri.hrms.dto.response.general.ErrorResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles all custom HrmsException instances with their specific ErrorType
    @ExceptionHandler(HrmsException.class)
    public ResponseEntity<ErrorResponseDTO> handleHrmsException(HrmsException ex) {
        ErrorType t = ex.getErrorType();
        return new ResponseEntity<>(new ErrorResponseDTO(t.getCode(), ex.getMessage()), t.getStatus());
    }

    // Handles Spring Security access denied exceptions (403)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleDenied(AccessDeniedException ex) {
        var t = ErrorType.AUTHORIZATION_ERROR;
        return new ResponseEntity<>(new ErrorResponseDTO(t.getCode(), ex.getMessage()), t.getStatus());
    }

    // Handles JPA EntityNotFoundException (404) if thrown directly
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleEntityNotFound(EntityNotFoundException ex) {
        var t = ErrorType.RESOURCE_NOT_FOUND;
        return new ResponseEntity<>(new ErrorResponseDTO(t.getCode(), ex.getMessage()), t.getStatus());
    }

    // Handles @Valid field validation errors (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var t = ErrorType.VALIDATION_ERROR;
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return new ResponseEntity<>(new ErrorResponseDTO(t.getCode(), msg), t.getStatus());
    }

    // Handles @Validated parameter constraint violations (400)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex) {
        var t = ErrorType.VALIDATION_ERROR;
        String msg = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        return new ResponseEntity<>(new ErrorResponseDTO(t.getCode(), msg), t.getStatus());
    }

    // Handles any other unexpected exceptions (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        var t = ErrorType.INTERNAL_ERROR;
        return new ResponseEntity<>(new ErrorResponseDTO(t.getCode(), t.getMessage()), t.getStatus());
    }
}
