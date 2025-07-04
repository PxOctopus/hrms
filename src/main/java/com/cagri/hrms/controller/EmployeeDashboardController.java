package com.cagri.hrms.controller;

import com.cagri.hrms.dto.response.general.DashboardResponseDTO;
import com.cagri.hrms.security.CustomUserDetails;
import com.cagri.hrms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee/dashboard")
@RequiredArgsConstructor
public class EmployeeDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponseDTO> getEmployeeDashboard(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long employeeId = currentUser.getId(); //  The ID of the employee who is currently logged in
        DashboardResponseDTO response = dashboardService.getEmployeeDashboard(employeeId);
        return ResponseEntity.ok(response);
    }
}
