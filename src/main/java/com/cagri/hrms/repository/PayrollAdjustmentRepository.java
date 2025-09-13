package com.cagri.hrms.repository;

import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.expense.PayrollAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollAdjustmentRepository extends JpaRepository<PayrollAdjustment, Long> {

    Page<PayrollAdjustment> findByEmployee(Employee employee, Pageable pageable);

    Page<PayrollAdjustment> findByProcessedFalse(Pageable pageable);
}
