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
@RequestMapping("/api/company/dashboard")
@RequiredArgsConstructor
public class CompanyDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponseDTO> getCompanyDashboard(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long companyId = currentUser.getCompanyId(); // User's company ID
        DashboardResponseDTO response = dashboardService.getCompanyDashboard(companyId);
        return ResponseEntity.ok(response);
    }
}
