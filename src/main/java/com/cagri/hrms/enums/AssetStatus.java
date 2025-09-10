package com.cagri.hrms.enums;

public enum AssetStatus {
    IN_STOCK,            // waiting in inventory
    ASSIGNED,            // assigned to employee (awaiting confirm)
    ASSIGNED_CONFIRMED,  // employee confirmed
    MAINTENANCE,         // in maintenance
    RETURN_REQUESTED,    // employee requested to return
    LOST,                // reported lost
    RETIRED              // decommissioned/retired
}
