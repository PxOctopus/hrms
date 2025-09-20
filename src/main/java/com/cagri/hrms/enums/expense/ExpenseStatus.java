package com.cagri.hrms.enums.expense;

/**
 * MVP workflow statuses for Expense.
 * Payment completion is tracked via `paidAt` (timestamp), not a separate PAID state.
 */
public enum ExpenseStatus {
    DRAFT,      // employee can edit
    SUBMITTED,  // waiting for manager review
    APPROVED,   // approved, will be reimbursed via payroll
    REJECTED,   // rejected by manager
    WITHDRAWN   // employee cancelled before payment
}