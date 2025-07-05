package com.cagri.hrms.dto.request.company;

import com.cagri.hrms.enums.EmployeeLimitLevel;
import com.cagri.hrms.enums.SubscriptionPlan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyRequestDTO {

    @NotBlank(message = "Company name must not be blank")
    private String companyName;

    @NotNull(message = "Employee limit level is required")
    private EmployeeLimitLevel employeeLimitLevel;

    @NotNull(message = "Employee number limit is required")
    private Integer employeeNumberLimit;

    @NotNull(message = "Subscription plan is required")
    private SubscriptionPlan subscriptionPlan;

}
