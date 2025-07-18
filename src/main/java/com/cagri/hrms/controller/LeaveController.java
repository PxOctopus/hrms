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

    // Allows MANAGER to request leave on behalf of an employee
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> requestLeave(@RequestBody LeaveRequestDTO dto) {
        leaveService.requestLeave(dto);
        return ResponseEntity.ok().build();
    }

    // Allows MANAGER to view all leave requests
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<LeaveResponseDTO>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    // Allows MANAGER to view leave requests of a specific employee
    @GetMapping("/by-employee/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    // When a MANAGER selects an employee from the frontend, the employee's ID is passed as a path variable.
    public ResponseEntity<List<LeaveResponseDTO>> getLeavesByEmployeeId(@PathVariable Long id) {
    // This ID is used to retrieve the leave records of that specific employee.
        return ResponseEntity.ok(leaveService.getLeavesByEmployeeId(id));
    }

    // Allows EMPLOYEE to view their own leave requests
    @GetMapping("/my-leaves")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<LeaveResponseDTO>> getMyLeaves() {
        return ResponseEntity.ok(leaveService.getLeavesOfCurrentEmployee());
    }

    // Allows MANAGER to approve or reject a leave request
    @PostMapping("/decision")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> approveOrRejectLeave(@RequestBody LeaveApprovalDTO dto) {
        leaveService.approveOrRejectLeave(dto);
        return ResponseEntity.ok().build();
    }
}
