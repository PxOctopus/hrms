package com.cagri.hrms.exception;

import org.springframework.http.ResponseEntity;
import com.cagri.hrms.dto.response.ErrorResponseDTO;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles all custom HRMS exceptions (e.g., ValidationException, BusinessException, etc.)
    @ExceptionHandler(HrmsException.class)
    public ResponseEntity<ErrorResponseDTO> handleHrmsException(HrmsException ex) {
        ErrorType errorType = ex.getErrorType();
        ErrorResponseDTO response = new ErrorResponseDTO(
                errorType.getCode(),
                errorType.getMessage()
        );
        return new ResponseEntity<>(response, errorType.getStatus());
    }

    // Handles all uncaught/unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        ErrorType errorType = ErrorType.INTERNAL_ERROR;
        ErrorResponseDTO response = new ErrorResponseDTO(
                errorType.getCode(),
                errorType.getMessage()
        );
        return new ResponseEntity<>(response, errorType.getStatus());
    }
}
