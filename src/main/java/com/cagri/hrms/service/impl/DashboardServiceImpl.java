package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.response.general.DashboardResponseDTO;
import com.cagri.hrms.repository.CompanyRepository;
import com.cagri.hrms.repository.EmployeeRepository;
import com.cagri.hrms.repository.UserRepository;
import com.cagri.hrms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;

    @Override
    public DashboardResponseDTO getAdminDashboard() {
        int userCount = (int) userRepository.count();
        int employeeCount = (int) employeeRepository.count();
        int activeCompanyCount = companyRepository.countByIsActiveTrue();

        return new DashboardResponseDTO(
                userCount, "Total registered users",
                employeeCount, "Total active employees",
                activeCompanyCount, "Total active companies"
        );
    }

    @Override
    public DashboardResponseDTO getCompanyDashboard(Long companyId) {
        int userCount = userRepository.countByCompanyId(companyId);
        int employeeCount = employeeRepository.countByCompanyId(companyId);
        int activeCompanyCount = companyRepository.existsByIdAndIsActiveTrue(companyId) ? 1 : 0;

        return new DashboardResponseDTO(
                userCount, "Users in this company",
                employeeCount, "Employees in this company",
                activeCompanyCount, "Company is active"
        );
    }

    @Override
    public DashboardResponseDTO getEmployeeDashboard(Long employeeId) {

        return new DashboardResponseDTO(
                0, "N/A for employee",
                1, "This employee",
                0, "N/A for employee"
        );
    }
}