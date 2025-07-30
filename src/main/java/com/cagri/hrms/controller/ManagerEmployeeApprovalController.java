package com.cagri.hrms.controller;

import com.cagri.hrms.dto.response.employee.EmployeeResponseDTO;
import com.cagri.hrms.dto.response.general.MessageResponseDTO;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.service.EmployeeService;
import com.cagri.hrms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager/employees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerEmployeeApprovalController {

    private final EmployeeService employeeService;
    private final UserService userService;

    // GET /api/manager/employees/pending
    @GetMapping("/pending")
    public ResponseEntity<List<EmployeeResponseDTO>> getPendingEmployees(
            @AuthenticationPrincipal UserDetails userDetails) {

        User manager = userService.getUserByEmail(userDetails.getUsername());
        List<EmployeeResponseDTO> pendingEmployees = employeeService.getPendingEmployeesForManager(manager);
        return ResponseEntity.ok(pendingEmployees);
    }

    // POST /api/manager/employees/{id}/approve
    @PostMapping("/{id}/approve")
    public ResponseEntity<MessageResponseDTO> approveEmployee(@PathVariable Long id) {
        employeeService.approveEmployee(id);
        return ResponseEntity.ok(new MessageResponseDTO("Employee approved successfully."));
    }

    // POST /api/manager/employees/{id}/reject
    @PostMapping("/{id}/reject")
    public ResponseEntity<MessageResponseDTO> rejectEmployee(@PathVariable Long id) {
        employeeService.rejectEmployee(id);
        return ResponseEntity.ok(new MessageResponseDTO("Employee rejected successfully."));
    }
}
