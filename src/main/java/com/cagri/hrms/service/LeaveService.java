package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.employee.LeaveApprovalDTO;
import com.cagri.hrms.dto.request.employee.LeaveCheckDTO;
import com.cagri.hrms.dto.request.employee.LeaveRequestDTO;
import com.cagri.hrms.dto.response.employee.LeaveResponseDTO;
import com.cagri.hrms.dto.response.employee.MyAllocationDTO;

import java.util.List;
import java.util.Map;

/**
 * Leave domain service.
 *
 * Notes:
 * - Quota logic (current setup):
 *   * Annual Leave → quota is derived from Employee.annualLeave (per year).
 *   * Sick/other non-annual → unlimited (still requires manager approval).
 * - Pre-check endpoints use {@link #checkOverlap(LeaveCheckDTO)} and {@link #checkAnnualQuota(LeaveCheckDTO)}.
 * - FE calls {@link #getMyAllocations()} to show remaining days in UI
 *   (this is synthesized from Employee.annualLeave; no separate allocation table required).
 */
public interface LeaveService {

    // --- Commands ---

    /** Submit a new leave request (EMPLOYEE for self → PENDING, MANAGER for employee → APPROVED). */
    void requestLeave(LeaveRequestDTO dto);

    /** Manager approves or rejects a pending leave. */
    void approveOrRejectLeave(LeaveApprovalDTO dto);


    // --- Queries (lists) ---

    /** Manager: get all leaves. */
    List<LeaveResponseDTO> getAllLeaves();

    /** Manager: get leaves of a specific employee. */
    List<LeaveResponseDTO> getLeavesByEmployeeId(Long employeeId);

    /** Employee: get own leaves. */
    List<LeaveResponseDTO> getLeavesOfCurrentEmployee();

    /** All approved leaves (for reporting if needed). */
    List<LeaveResponseDTO> getApprovedLeaves();

    /** All pending leaves (for reporting if needed). */
    List<LeaveResponseDTO> getPendingLeaves();

    /** All rejected leaves (for reporting if needed). */
    List<LeaveResponseDTO> getRejectedLeaves();

    /** Manager: leaves I created/assigned. */
    List<LeaveResponseDTO> getLeavesAssignedByManager();

    /** Manager: pending leaves in my company waiting for my decision. */
    List<LeaveResponseDTO> getLeavesWaitingForMyApproval();

    /** Manager: leaves I approved. */
    List<LeaveResponseDTO> getLeavesApprovedByManager();


    // --- Lightweight pre-checks used by live UI badges ---

    /** Returns true if requested date range overlaps with an existing (blocking) leave. */
    boolean checkOverlap(LeaveCheckDTO dto);

    /**
     * Quota pre-check.
     * - For non-annual types returns { ok: true }.
     * - For annual, returns { ok: boolean, remainingDays: number } based on Employee.annualLeave & used days.
     */
    Map<String, Object> checkAnnualQuota(LeaveCheckDTO dto);


    // --- Allocations for FE (employee’s own) ---

    /**
     * Employee: list of active “allocations” for the current year with
     * totalDays (allocated) and usedDays (approved usage) per leaveDefinition.
     * In the current setup this is synthesized from Employee.annualLeave.
     */
    List<MyAllocationDTO> getMyAllocations();
}
