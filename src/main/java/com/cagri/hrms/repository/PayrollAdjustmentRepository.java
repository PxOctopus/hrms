package com.cagri.hrms.repository;

import com.cagri.hrms.dto.response.expense.PayrollAdjustmentDTO;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.expense.PayrollAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface PayrollAdjustmentRepository extends JpaRepository<PayrollAdjustment, Long> {

    // Employee-facing list of their payroll adjustments (e.g., reimbursements)
    Page<PayrollAdjustment> findByEmployee(Employee employee, Pageable pageable);

    // Batch processing queue (unprocessed adjustments)
    Page<PayrollAdjustment> findByProcessedFalse(Pageable pageable);

//    Page<PayrollAdjustment> findByEmployeeIdOrderByEffectiveDateDesc(Long employeeId, Pageable pageable);
//    // --- DTO projection  ---
//    @Query("""
//       select new com.cagri.hrms.dto.response.expense.PayrollAdjustmentDTO(
//          p.id, p.type, p.amount, p.currency, p.description,
//          p.effectiveDate, p.processed, p.expenseId, p.createdAt
//       )
//       from PayrollAdjustment p
//       where p.employee.id = :employeeId
//       order by p.effectiveDate desc
//    """)
    Page<PayrollAdjustmentDTO> findDTOsByEmployeeId(@Param("employeeId") Long employeeId, Pageable pageable);

    Optional<PayrollAdjustment> findByExpenseId(Long expenseId);

    boolean existsByExpenseId(Long expenseId);

    Page<PayrollAdjustment> findByEmployeeIdAndProcessedTrueOrderByEffectiveDateDesc(
            Long employeeId, Pageable pageable
    );


}
