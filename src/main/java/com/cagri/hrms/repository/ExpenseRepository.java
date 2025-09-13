package com.cagri.hrms.repository;

import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.expense.Expense;
import com.cagri.hrms.entity.expense.Project;
import com.cagri.hrms.enums.expense.ExpenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Page<Expense> findByEmployee(Employee employee, Pageable pageable);

    Page<Expense> findByProjectCompanyIdAndStatus(Long companyId, ExpenseStatus status, Pageable pageable);

    Page<Expense> findByProjectAndStatus(Project project, ExpenseStatus status, Pageable pageable);
}
