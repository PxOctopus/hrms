package com.cagri.hrms.controller;

import com.cagri.hrms.dto.response.expense.PayrollAdjustmentDTO;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.service.CompanyService;
import com.cagri.hrms.service.EmployeeService;
import com.cagri.hrms.service.PayrollService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * PayrollController: Employee can view their payroll adjustments (e.g., reimbursements).
 */
@RestController
@RequestMapping("/api/payroll/adjustments")
@RequiredArgsConstructor
public class PayrollController {
    private final CompanyService companyService;
    private final EmployeeService employeeService;
    private final PayrollService service;

    private Employee currentEmployee() {
        Long userId = companyService.getCurrentUserId();
        return employeeService.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/my")
    public Page<PayrollAdjustmentDTO> myAdjustments(@PageableDefault(size = 10) Pageable pageable) {
        Long userId = companyService.getCurrentUserId();
        Long empId = employeeService.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"))
                .getId();

        return service.findProcessedByEmployee(empId, pageable);
    }

}
