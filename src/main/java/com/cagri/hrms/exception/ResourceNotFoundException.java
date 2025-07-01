package com.cagri.hrms.exception;

public class ResourceNotFoundException extends HrmsException {
    public ResourceNotFoundException() {
        super(ErrorType.RESOURCE_NOT_FOUND);
    }
}
