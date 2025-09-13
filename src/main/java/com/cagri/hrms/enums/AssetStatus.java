package com.cagri.hrms.enums;

public enum AssetStatus {
    IN_STOCK,             // available in inventory
    ASSIGNED,             // assigned to an employee (awaiting confirm)
    ASSIGNED_CONFIRMED,   // employee confirmed receipt
    RETURN_REQUESTED,     // employee requested to return
    MAINTENANCE,          // in maintenance / repair
    LOST,                 // reported lost

    // Retirement is a manager-approved flow:
    RETIRE_REQUESTED,     // employee requested retirement (pending approval)
    RETIRED               // decommissioned/retired (terminal; not assignable)
}
