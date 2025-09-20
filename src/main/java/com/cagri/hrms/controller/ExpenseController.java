// src/main/java/com/cagri/hrms/controller/ExpenseController.java
package com.cagri.hrms.controller;

import com.cagri.hrms.dto.request.expense.ExpenseCreateDTO;
import com.cagri.hrms.dto.request.expense.ExpenseUpdateDTO;
import com.cagri.hrms.dto.request.expense.RejectRequestDTO;
import com.cagri.hrms.dto.response.expense.ExpenseResponseDTO;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.enums.expense.ExpenseStatus;
import com.cagri.hrms.service.CompanyService;
import com.cagri.hrms.service.EmployeeService;
import com.cagri.hrms.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * MVP without Project.
 * Employee endpoints use currentEmployee(); Manager endpoints use company scope.
 */
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService service;
    private final CompanyService companyService;
    private final EmployeeService employeeService;

    /** Resolve current employee from authenticated user (MANAGERs don't have Employee profile). */
    private Employee currentEmployee() {
        Long userId = companyService.getCurrentUserId();
        return employeeService.findByUserId(userId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Employee not found for current user"));
    }

    /** Resolve current company safely (works for MANAGER or EMPLOYEE). */
    private Long currentCompanyId() {
        return companyService.getCurrentCompanyIdOrThrow();
    }

    // ----- Employee endpoints -----

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/my")
    public Page<ExpenseResponseDTO> myExpenses(@PageableDefault(size = 20) Pageable pageable) {
        return service.listMy(currentEmployee(), pageable);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping
    public ExpenseResponseDTO create(@Valid @RequestBody ExpenseCreateDTO dto) {
        return service.create(dto, currentEmployee());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PutMapping("/{id:\\d+}")
    public ExpenseResponseDTO update(@PathVariable("id") Long id, @Valid @RequestBody ExpenseUpdateDTO dto) {
        return service.update(id, dto, currentEmployee());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{id:\\d+}/submit")
    public ExpenseResponseDTO submit(@PathVariable("id") Long id) {
        return service.submit(id, currentEmployee());
    }

    // IMPORTANT: restrict to EMPLOYEE to avoid resolving an employee for manager users
    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/{id:\\d+}")
    public ExpenseResponseDTO get(@PathVariable("id") Long id) {
        return service.getById(id, currentEmployee());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @DeleteMapping("/{id:\\d+}")
    public void delete(@PathVariable("id") Long id) {
        service.delete(id, currentEmployee());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{id:\\d+}/withdraw")
    public ExpenseResponseDTO withdraw(@PathVariable("id") Long id) {
        return service.withdraw(id, currentEmployee());
    }

    // ----- Manager review endpoints -----

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/review")
    public Page<ExpenseResponseDTO> reviewQueue(
            @RequestParam(value = "status", required = false) ExpenseStatus status,
            @PageableDefault(size = 20, sort = "submittedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long companyId = currentCompanyId();
        // If status is null, service will return ALL for the company (SUBMITTED/APPROVED/REJECTED...)
        return service.findCompanyExpenses(companyId, status, pageable);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/{id:\\d+}/approve")
    public ExpenseResponseDTO approve(@PathVariable("id") Long id) {
        Long managerUserId = companyService.getCurrentUserId();
        return service.approve(id, managerUserId);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/{id:\\d+}/reject")
    public ExpenseResponseDTO reject(@PathVariable("id") Long id, @Valid @RequestBody RejectRequestDTO dto) {
        Long managerUserId = companyService.getCurrentUserId();
        return service.reject(id, dto.getReason(), managerUserId);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/{id:\\d+}/mark-paid")
    public ExpenseResponseDTO markPaid(@PathVariable Long id) {
        Long financeUserId = companyService.getCurrentUserId();
        return service.markPaid(id, financeUserId);
    }
}
