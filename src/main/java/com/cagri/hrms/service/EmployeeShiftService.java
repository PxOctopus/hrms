package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.employee.EmployeeShiftRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeShiftResponseDTO;
import com.cagri.hrms.dto.response.employee.EmployeeShiftWeekItemDTO;
import com.cagri.hrms.entity.core.User;

import java.time.LocalDate;
import java.util.List;

/**
 * Service boundary for EmployeeShift (dated assignments).
 * Company scoping MUST be enforced using the authenticated User (currentUser).
 *
 * Business rules handled by the implementation:
 *  - Company guard: employee and shift must belong to currentUser.company
 *  - Leave guard: block assignment if the day (and, for overnight, the next day) is on APPROVED leave
 *  - Duplicate guard: one assignment per (employeeId, shiftDate)
 *  - Overnight shift: endTime <= startTime means shift spans to the next calendar day (+1d in UI)
 */
public interface EmployeeShiftService {

    /**
     * Create a single-day assignment.
     * If the referenced Shift is "overnight" (endTime <= startTime), service will still create ONE record
     * for the provided shiftDate and apply leave checks for BOTH shiftDate and shiftDate+1.
     */
    EmployeeShiftResponseDTO assignEmployeeShift(EmployeeShiftRequestDTO request, User currentUser);

    /**
     * Update an existing single-day assignment (idempotent business rules apply).
     * Same guards as assign().
     */
    EmployeeShiftResponseDTO updateEmployeeShift(Long id, EmployeeShiftRequestDTO request, User currentUser);

    /** Hard delete (or implement soft-delete in the impl). Company guard still applies. */
    void deleteEmployeeShift(Long id, User currentUser);

    /** Read a single assignment by id (company guard applies). */
    EmployeeShiftResponseDTO getEmployeeShiftById(Long id, User currentUser);

    /**
     * List all assignments for an employee (usually avoided at scale).
     * Prefer the ranged variant below for weekly views and 40h calculations.
     */
    List<EmployeeShiftResponseDTO> getEmployeeShiftsByEmployeeId(Long employeeId, User currentUser);

    // =========================
    // NEW: Weekly/Range listing for grid & 40h indicator
    // =========================

    /**
     * NEW: List active assignments for an employee within [from, to] (inclusive).
     * Returns denormalized time data (start/end) via EmployeeShiftWeekItemDTO to avoid client-side N+1.
     * Company guard applies.
     */
    List<EmployeeShiftWeekItemDTO> getEmployeeShiftsInRange(
            Long employeeId,
            LocalDate from,
            LocalDate to,
            User currentUser // used to enforce company scope
    ); // NEW
}