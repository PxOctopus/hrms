package com.cagri.hrms.controller;

import com.cagri.hrms.dto.request.employee.EmployeeShiftRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeShiftResponseDTO;
import com.cagri.hrms.dto.response.employee.EmployeeShiftWeekItemDTO;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.service.EmployeeShiftService;
import com.cagri.hrms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Employee shift assignments (dated). Overnight/leave/duplicate rules live in the service.
 */
@RestController
@RequestMapping("/api/employee-shifts")
@RequiredArgsConstructor
public class EmployeeShiftController {

    private final EmployeeShiftService employeeShiftService;
    private final UserService userService;

    /** Managers only: create a single-day assignment for an employee. */
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<EmployeeShiftResponseDTO> assign(
            @Valid @RequestBody EmployeeShiftRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(employeeShiftService.assignEmployeeShift(dto, currentUser));
    }

    /** Managers only: update an existing assignment. */
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeShiftResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeShiftRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(employeeShiftService.updateEmployeeShift(id, dto, currentUser));
    }

    /** Managers only: soft delete an assignment. */
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        employeeShiftService.deleteEmployeeShift(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    /** Manager (same company) or the employee themselves can read a single assignment. */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeShiftResponseDTO> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(employeeShiftService.getEmployeeShiftById(id, currentUser));
    }

    /** Manager (same company) or self: list all assignments for a given employee (use sparingly). */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<EmployeeShiftResponseDTO>> listByEmployee(
            @RequestParam Long employeeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(employeeShiftService.getEmployeeShiftsByEmployeeId(employeeId, currentUser));
    }

    // ---------- Weekly / Range endpoint (for the grid & 40h chip) ----------

    /** Managers only (same company): list active assignments within [from, to] inclusive. */
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/range")
    public ResponseEntity<List<EmployeeShiftWeekItemDTO>> listInRange(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(employeeShiftService.getEmployeeShiftsInRange(employeeId, from, to, currentUser));
    }
}
