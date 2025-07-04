package com.cagri.hrms.controller;

import com.cagri.hrms.dto.response.general.DashboardResponseDTO;
import com.cagri.hrms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponseDTO> getAdminDashboard() {
        DashboardResponseDTO response = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(response);
    }
}
