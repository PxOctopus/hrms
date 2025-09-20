package com.cagri.hrms.repository;

import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.expense.Expense;
import com.cagri.hrms.enums.expense.ExpenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * MVP without Project
 * Queries are scoped by employee or by employee's company.
 */
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Employee-facing lists (e.g., "My expenses")
    Page<Expense> findByEmployee(Employee employee, Pageable pageable);

    // Manager review queue for a company
    Page<Expense> findByEmployeeCompanyIdAndStatus(Long companyId,
                                                   ExpenseStatus status,
                                                   Pageable pageable);

    // ALL (no status filter)
    Page<Expense> findByEmployeeCompanyId(Long companyId, Pageable pageable);

    // Safety helpers for fetching a single expense in the right scope
    Optional<Expense> findByIdAndEmployee(Long id, Employee employee);          // employee-owned
    Optional<Expense> findByIdAndEmployeeCompanyId(Long id, Long companyId);    // company-owned (manager scope)
}
