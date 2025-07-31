package com.cagri.hrms.util;

import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.exception.BusinessException;

public class UserValidator {

    private UserValidator() {
        // Utility class: private constructor to prevent instantiation
    }

    public static void assertCompanyApproved(User user) {
        if (user.getCompany() == null) {
            throw new BusinessException("You must be associated with an approved company to access this resource.");
        }
    }
}

