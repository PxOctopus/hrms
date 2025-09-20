package com.cagri.hrms.service;

import com.cagri.hrms.dto.response.expense.PayrollAdjustmentDTO;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.expense.PayrollAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payroll service for expense reimbursements.
 * MVP: create a simple reimbursement adjustment on approval.
 */
public interface PayrollService {

    /**
     * Create a reimbursement adjustment for an approved expense.
     * For MVP, we pass amount in TRY and effective date.
     */
    PayrollAdjustment createReimbursementForExpense(Long expenseId,
                                                    Employee employee,
                                                    BigDecimal amountTRY,
                                                    LocalDate effectiveDate);

    /** List payroll adjustments of the given employee. */
    public Page<PayrollAdjustmentDTO> listMyAdjustments(Employee me, Pageable pageable);

    /** Optional batch hook: mark adjustments processed up to a payroll date. */
    int markProcessedUpTo(LocalDate payrollDate);

    Page<PayrollAdjustmentDTO> findProcessedByEmployee(Long empId, Pageable pageable);


}
