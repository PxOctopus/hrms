package com.cagri.hrms.exception;


public class AuthenticationException extends HrmsException {
    public AuthenticationException() {
        super(ErrorType.AUTHENTICATION_ERROR);
    }
}
