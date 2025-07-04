package com.cagri.hrms.dto.response.general;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardResponseDTO {
    private int userCount;              // Total number of users in the system
    private String userLabel;           // Description of userCount

    private int employeeCount;          // Total number of employees in the system
    private String employeeLabel;       // Description of employeeCount

    private int activeCompanyCount;     // Number of currently active companies
    private String companyLabel;        // Description of activeCompanyCount
}
