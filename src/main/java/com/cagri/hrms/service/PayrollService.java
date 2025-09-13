package com.cagri.hrms.service;

import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.expense.PayrollAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PayrollService {

    // Create reimbursement adjustment when an expense is approved
    PayrollAdjustment createReimbursementForExpense(Long expenseId, Employee employee, BigDecimal amountTRY, String originalCurrency, LocalDate effectiveDate);

    Page<PayrollAdjustment> listMyAdjustments(Employee employee, Pageable pageable);

    // Batch job hook to mark adjustments processed (optional)
    int markProcessedUpTo(LocalDate payrollDate);
}
