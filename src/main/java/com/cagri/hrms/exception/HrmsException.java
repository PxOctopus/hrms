package com.cagri.hrms.exception;

import lombok.Getter;

@Getter
public class HrmsException extends RuntimeException {
    private final ErrorType errorType;

    public HrmsException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }


}
