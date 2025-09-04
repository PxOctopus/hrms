package com.cagri.hrms.controller;

import com.cagri.hrms.dto.request.employee.LeaveApprovalDTO;
import com.cagri.hrms.dto.request.employee.LeaveCheckDTO;
import com.cagri.hrms.dto.request.employee.LeaveRequestDTO;
import com.cagri.hrms.dto.response.employee.LeaveResponseDTO;
import com.cagri.hrms.dto.response.employee.MyAllocationDTO;
import com.cagri.hrms.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * LeaveController for handling leave-related operations.
 *
 * EMPLOYEE:
 *  - Can create leave only for themselves.
 *  - Can view their own leaves.
 *
 * MANAGER:
 *  - Can create leave only for other employees (not themselves).
 *  - Can view all leaves or by employee.
 *  - Can approve/reject leave requests.
 */
@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    // ---- Live pre-check endpoints (used by UI badges) ----

    @PostMapping("/check/overlap")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public ResponseEntity<Boolean> checkOverlap(@RequestBody LeaveCheckDTO dto) {
        boolean overlap = leaveService.checkOverlap(dto);
        return ResponseEntity.ok(overlap);
    }

    @PostMapping("/check/quota")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public ResponseEntity<Map<String, Object>> checkQuota(@RequestBody LeaveCheckDTO dto) {
        Map<String, Object> result = leaveService.checkAnnualQuota(dto); // { ok, remainingDays? }
        return ResponseEntity.ok(result);
    }

    // ---- Core flows ----

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public ResponseEntity<Void> requestLeave(@RequestBody LeaveRequestDTO dto) {
        leaveService.requestLeave(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/decision")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> approveOrRejectLeave(@RequestBody LeaveApprovalDTO dto) {
        leaveService.approveOrRejectLeave(dto);
        return ResponseEntity.ok().build();
    }

    // ---- Queries ----

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<LeaveResponseDTO>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @GetMapping("/assigned-by-me")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<LeaveResponseDTO>> getLeavesAssignedByManager() {
        return ResponseEntity.ok(leaveService.getLeavesAssignedByManager());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<LeaveResponseDTO>> getPendingLeaves() {
        return ResponseEntity.ok(leaveService.getLeavesWaitingForMyApproval());
    }

    @GetMapping("/approved-by-me")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<LeaveResponseDTO>> getLeavesApprovedByMe() {
        return ResponseEntity.ok(leaveService.getLeavesApprovedByManager());
    }

    @GetMapping("/by-employee/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<LeaveResponseDTO>> getLeavesByEmployeeId(@PathVariable Long id) {
        return ResponseEntity.ok(leaveService.getLeavesByEmployeeId(id));
    }

    @GetMapping("/my-leaves")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<LeaveResponseDTO>> getMyLeaves() {
        return ResponseEntity.ok(leaveService.getLeavesOfCurrentEmployee());
    }

    // ----  Allocations for employee (synthesized from Employee.annualLeave) ----
    @GetMapping("/allocations/me")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<MyAllocationDTO>> myAllocations() {
        return ResponseEntity.ok(leaveService.getMyAllocations());
    }
}
