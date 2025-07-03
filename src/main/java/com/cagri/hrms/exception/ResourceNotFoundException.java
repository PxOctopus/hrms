package com.cagri.hrms.exception;

public class ResourceNotFoundException extends HrmsException {
    public ResourceNotFoundException(String message) {
        super(ErrorType.RESOURCE_NOT_FOUND, message);
    }

}
