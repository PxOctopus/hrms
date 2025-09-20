package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.response.expense.PayrollAdjustmentDTO;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.expense.PayrollAdjustment;
import com.cagri.hrms.enums.expense.PayrollAdjustmentType;
import com.cagri.hrms.mapper.PayrollAdjustmentMapper;
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
    private final PayrollAdjustmentMapper adjMapper;


    @Override
    public PayrollAdjustment createReimbursementForExpense(Long expenseId,
                                                           Employee employee,
                                                           BigDecimal amountTRY,
                                                           LocalDate effectiveDate) {
        // MVP: reimburse in TRY; currency fixed as "TRY"
        PayrollAdjustment pa = PayrollAdjustment.builder()
                .employee(employee)
                .type(PayrollAdjustmentType.REIMBURSEMENT)
                .amount(amountTRY)
                .currency("TRY")
                .description("Expense #" + expenseId + " reimbursement")
                .effectiveDate(effectiveDate)
                .expenseId(expenseId)
                .processed(false)
                .build();
        return repo.save(pa);
    }

    public Page<PayrollAdjustmentDTO> listMyAdjustments(Employee me, Pageable pageable) {
        return repo.findDTOsByEmployeeId(me.getId(), pageable);
    }

    @Override
    public int markProcessedUpTo(LocalDate payrollDate) {
        // TODO: Implement batch processing that marks adjustments as processed
        // based on the given payroll date.
        return 0;
    }
    @Override
    public Page<PayrollAdjustmentDTO> findProcessedByEmployee(Long empId, Pageable pageable) {
        return repo
                .findByEmployeeIdAndProcessedTrueOrderByEffectiveDateDesc(empId, pageable)
                .map(adjMapper::toDTO);
    }


}
