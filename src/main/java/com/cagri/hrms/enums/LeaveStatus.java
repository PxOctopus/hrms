package com.cagri.hrms.enums;

import jakarta.persistence.EnumType;


public enum LeaveStatus {
    PENDING,    // Awaiting manager approval
    APPROVED,   // Approved by manager
    REJECTED    // Rejected by manager
}
