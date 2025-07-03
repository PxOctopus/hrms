package com.cagri.hrms.exception;


public class BusinessException extends HrmsException {
    public BusinessException(String message) {
        super(ErrorType.BUSINESS_ERROR, message);
    }
}
