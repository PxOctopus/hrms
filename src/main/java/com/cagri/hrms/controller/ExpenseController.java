package com.cagri.hrms.controller;

import com.cagri.hrms.dto.request.employee.ExpenseRequestDTO;
import com.cagri.hrms.dto.response.employee.ExpenseResponseDTO;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.service.ExpenseService;
import com.cagri.hrms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    private final ExpenseService expenseService;
    private final UserService userService;

    // Create expense (EMPLOYEE only, for themselves)
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ExpenseResponseDTO> createExpense(
            @Valid @RequestBody ExpenseRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(expenseService.createExpense(dto, currentUser));
    }

    // Update expense (EMPLOYEE only, for themselves)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ExpenseResponseDTO> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(expenseService.updateExpense(id, dto, currentUser));
    }

    // Delete expense (EMPLOYEE only, for themselves)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        expenseService.deleteExpense(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    // Get an expense by ID (EMPLOYEE or MANAGER)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public ResponseEntity<ExpenseResponseDTO> getExpenseById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(expenseService.getExpenseById(id, currentUser));
    }

    // Get all expenses of an employee (EMPLOYEE: self, MANAGER: company employees)
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesByEmployeeId(
            @PathVariable Long employeeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(expenseService.getExpensesByEmployeeId(employeeId, currentUser));
    }

    // Get all expenses in the company (MANAGER only)
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ExpenseResponseDTO>> getAllExpenses(
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(expenseService.getAllExpenses(currentUser));
    }

    // Approve expense (MANAGER only)
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> approveExpense(
            @PathVariable Long id,
            @RequestParam(required = false) String managerNote,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        expenseService.approveExpense(id, managerNote, currentUser);
        return ResponseEntity.ok().build();
    }

    // Reject expense (MANAGER only)
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> rejectExpense(
            @PathVariable Long id,
            @RequestParam(required = false) String managerNote,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        expenseService.rejectExpense(id, managerNote, currentUser);
        return ResponseEntity.ok().build();
    }
}
