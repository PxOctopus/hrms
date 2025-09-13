package com.cagri.hrms.controller;

import com.cagri.hrms.dto.request.employee.ExpenseRequestDTO;
import com.cagri.hrms.dto.request.expense.ExpenseCreateDTO;
import com.cagri.hrms.dto.request.expense.ExpenseUpdateDTO;
import com.cagri.hrms.dto.request.expense.RejectRequestDTO;
import com.cagri.hrms.dto.response.expense.ExpenseResponseDTO;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.service.CompanyService;
import com.cagri.hrms.service.ExpenseService;
import com.cagri.hrms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService service;
    private final CompanyService companyService;

    private Employee currentEmployee() { /* ... */
        return null;
    }

    private Long currentCompanyId() { /* ... */
        return null;
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
    @PutMapping("/{id}")
    public ExpenseResponseDTO update(@PathVariable Long id, @Valid @RequestBody ExpenseUpdateDTO dto) {
        return service.update(id, dto, currentEmployee());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{id}/submit")
    public ExpenseResponseDTO submit(@PathVariable Long id) {
        return service.submit(id, currentEmployee());
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    @GetMapping("/{id}")
    public ExpenseResponseDTO get(@PathVariable Long id) {
        return service.getById(id, currentEmployee());
    }

    // ----- Manager review endpoints -----

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/review")
    public Page<ExpenseResponseDTO> reviewQueue(@PageableDefault(size = 20) Pageable pageable) {
        return service.listSubmittedForCompany(currentCompanyId(), pageable);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/{id}/approve")
    public ExpenseResponseDTO approve(@PathVariable Long id) {
        Long managerUserId = companyService.getCurrentUserId();
        return service.approve(id, managerUserId);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/{id}/reject")
    public ExpenseResponseDTO reject(@PathVariable Long id, @Valid @RequestBody RejectRequestDTO dto) {
        Long managerUserId = companyService.getCurrentUserId();
        return service.reject(id, dto.getReason(), managerUserId);
    }
}
