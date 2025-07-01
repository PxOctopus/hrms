package com.cagri.hrms.exception;


public class BusinessException extends HrmsException {
    public BusinessException() {
        super(ErrorType.BUSINESS_ERROR);
    }
}
