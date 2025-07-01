package com.cagri.hrms.exception;

public class ValidationException extends HrmsException {
    public ValidationException() {
        super(ErrorType.VALIDATION_ERROR);
    }
}