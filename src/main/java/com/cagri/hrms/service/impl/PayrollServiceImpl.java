package com.cagri.hrms.service.impl;

import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.expense.PayrollAdjustment;
import com.cagri.hrms.enums.expense.PayrollAdjustmentType;
import com.cagri.hrms.repository.PayrollAdjustmentRepository;
import com.cagri.hrms.service.PayrollService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollServiceImpl implements PayrollService {

    private final PayrollAdjustmentRepository repo;

    @Override
    public PayrollAdjustment createReimbursementForExpense(Long expenseId, Employee employee, BigDecimal amountTRY, String originalCurrency, LocalDate effectiveDate) {
        PayrollAdjustment pa = PayrollAdjustment.builder()
                .employee(employee)
                .type(PayrollAdjustmentType.REIMBURSEMENT)
                .amount(amountTRY)
                .currency(originalCurrency)
                .description("Expense #" + expenseId + " reimbursement")
                .effectiveDate(effectiveDate)
                .expenseId(expenseId)
                .processed(false)
                .build();
        return repo.save(pa);
    }

    @Override
    public Page<PayrollAdjustment> listMyAdjustments(Employee employee, Pageable pageable) {
        return repo.findByEmployee(employee, pageable);
    }

    @Override
    public int markProcessedUpTo(LocalDate payrollDate) {
        // TODO: implement batch job to mark processed based on payroll run
        return 0;
    }
}
