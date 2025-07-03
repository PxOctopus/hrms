package com.cagri.hrms.dto.response.company;

import com.cagri.hrms.enums.EmployeeLimitLevel;
import com.cagri.hrms.enums.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompanyResponseDTO {

    private Long id;
    private String companyName;
    private Integer numberOfEmployees;
    private EmployeeLimitLevel employeeLimitLevel;
    private Integer employeeNumberLimit;
    private boolean isActive;
    private String managerFullName;
    private SubscriptionPlan subscriptionPlan;
    private Long createdAt;
    private Long updatedAt;
}
