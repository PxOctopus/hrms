package com.cagri.hrms.service;

import com.cagri.hrms.dto.response.general.DashboardResponseDTO;

public interface DashboardService {
    DashboardResponseDTO getAdminDashboard();
    DashboardResponseDTO getCompanyDashboard(Long companyId);
    DashboardResponseDTO getEmployeeDashboard(Long employeeId);
}
