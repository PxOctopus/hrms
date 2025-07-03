package com.cagri.hrms.exception;

public class ValidationException extends HrmsException {
    public ValidationException(String message) {
        super(ErrorType.VALIDATION_ERROR, message);
    }
}