package com.cagri.hrms.controller;

import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.expense.PayrollAdjustment;
import com.cagri.hrms.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService service;

    private Employee currentEmployee() { /* ... */ return null; }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/adjustments/my")
    public Page<PayrollAdjustment> myAdjustments(@PageableDefault(size = 20) Pageable pageable) {
        return service.listMyAdjustments(currentEmployee(), pageable);
    }
}
