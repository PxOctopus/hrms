package com.cagri.hrms.controller;

import com.cagri.hrms.dto.request.employee.LeaveApprovalDTO;
import com.cagri.hrms.dto.request.employee.LeaveRequestDTO;
import com.cagri.hrms.dto.response.employee.LeaveResponseDTO;
import com.cagri.hrms.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    /**
     * Allows both EMPLOYEE and MANAGER to request leave.
     * - EMPLOYEE can request leave only for themselves.
     * - MANAGER can request leave for any employee (including themselves).
     * The service layer checks and enforces these business rules.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public ResponseEntity<Void> requestLeave(@RequestBody LeaveRequestDTO dto) {
        leaveService.requestLeave(dto);
        return ResponseEntity.ok().build();
    }

    // Only MANAGER can view all leave requests
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<LeaveResponseDTO>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    // Only MANAGER can view leave requests of a specific employee
    @GetMapping("/by-employee/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<LeaveResponseDTO>> getLeavesByEmployeeId(@PathVariable Long id) {
        return ResponseEntity.ok(leaveService.getLeavesByEmployeeId(id));
    }

    // EMPLOYEE can view their own leaves
    @GetMapping("/my-leaves")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<LeaveResponseDTO>> getMyLeaves() {
        return ResponseEntity.ok(leaveService.getLeavesOfCurrentEmployee());
    }

    // Only MANAGER can approve or reject leave
    @PostMapping("/decision")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> approveOrRejectLeave(@RequestBody LeaveApprovalDTO dto) {
        leaveService.approveOrRejectLeave(dto);
        return ResponseEntity.ok().build();
    }
}
