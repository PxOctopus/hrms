package com.cagri.hrms.exception;

public class AuthorizationException extends HrmsException {
    public AuthorizationException() {
        super(ErrorType.AUTHORIZATION_ERROR);
    }
}
