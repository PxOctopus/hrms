package com.cagri.hrms.exception;


public class AuthenticationException extends HrmsException {
    public AuthenticationException(String message) {
        super(ErrorType.AUTHENTICATION_ERROR, message);
    }
}
