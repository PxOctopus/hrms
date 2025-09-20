package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.expense.ExpenseCreateDTO;
import com.cagri.hrms.dto.request.expense.ExpenseUpdateDTO;
import com.cagri.hrms.dto.response.expense.ExpenseResponseDTO;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.enums.expense.ExpenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Expense service for MVP (no Project).
 * Employee scope: create/update/get/list/submit/withdraw/delete.
 * Manager scope: list submitted for company, approve, reject.
 */
public interface ExpenseService {

    /** Create a new expense owned by the given employee. */
    ExpenseResponseDTO create(ExpenseCreateDTO dto, Employee employee);

    /** Update an existing expense (only allowed in DRAFT/REJECTED and by owner). */
    ExpenseResponseDTO update(Long id, ExpenseUpdateDTO dto, Employee employee);

    /** Get a single expense in employee scope (ensures ownership). */
    ExpenseResponseDTO getById(Long id, Employee requester);

    /** List expenses for the current employee. */
    Page<ExpenseResponseDTO> listMy(Employee employee, Pageable pageable);

    /** Submit an expense for manager review. */
    ExpenseResponseDTO submit(Long id, Employee employee);

    /** Manager view: list SUBMITTED expenses for the manager's company. */
    Page<ExpenseResponseDTO> listSubmittedForCompany(Long companyId, Pageable pageable);

    /** Manager action: approve an expense (will create a payroll adjustment). */
    ExpenseResponseDTO approve(Long id, Long managerUserId);

    /** Manager action: reject an expense with a reason. */
    ExpenseResponseDTO reject(Long id, String reason, Long managerUserId);

    /** Compute UI permissions for the caller (owner vs manager). */
    ExpenseResponseDTO withAllowedActions(ExpenseResponseDTO dto, Employee requester, boolean isManager);

    /** Delete an expense (owner-only, typically only if DRAFT/REJECTED/WITHDRAWN). */
    void delete(Long id, Employee employee);

    /** Withdraw an expense (owner-only, while not approved/paid). */
    ExpenseResponseDTO withdraw(Long id, Employee employee);

    Page<ExpenseResponseDTO> findCompanyExpenses(Long companyId, ExpenseStatus status, Pageable pageable);

    ExpenseResponseDTO markPaid(Long expenseId, Long financeUserId);

}
