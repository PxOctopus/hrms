package com.cagri.hrms.exception;

public class AuthorizationException extends HrmsException {
    public AuthorizationException(String message) {
        super(ErrorType.AUTHORIZATION_ERROR, message);
    }
}
