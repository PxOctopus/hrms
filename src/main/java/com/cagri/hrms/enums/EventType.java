package com.cagri.hrms.enums;

/**
 * Domain events for assets; used for audit trails and UI rules.
 */
public enum EventType {
    ASSIGNED,
    CONFIRMED,

    RETURN_REQUESTED,
    RETURN_COMPLETED,
    RETURN_REQUEST_CANCELED,

    STATUS_CHANGED,
    CONDITION_CHANGED,

    MAINTENANCE_OPENED,
    MAINTENANCE_CLOSED,

    ISSUE_REPORTED,
    ISSUE_CONFIRMED,
    ISSUE_REPORT_CANCELED,

    LOST_REPORTED,

    // Retirement (manager-approved)
    RETIREMENT_REQUESTED,   // employee requested retirement
    RETIREMENT_APPROVED,    // manager approved -> asset becomes RETIRED

    // Soft delete / archive
    ASSET_ARCHIVED,

    // CRUD-ish audit events
    ASSET_CREATED,
    ASSET_UPDATED
}
