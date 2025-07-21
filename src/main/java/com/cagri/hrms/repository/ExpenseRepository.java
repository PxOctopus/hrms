package com.cagri.hrms.repository;

import com.cagri.hrms.entity.employee.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findAllByEmployee_Id(Long employeeId);
}
