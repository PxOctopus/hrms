package com.cagri.hrms.dto.request.employee;

import com.cagri.hrms.enums.ContractType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeCreateRequestDTO {

    private LocalDate birthDate;
    private LocalDate hireDate;
    private LocalDate endDate;

    @NotBlank(message = "Position is required")
    private String position;

    @NotNull(message = "Contract type is required")
    private ContractType contractType;

    private String phoneNumber;
    private String address;

    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be positive")
    private BigDecimal salary;

    @Min(value = 0, message = "Annual leave must be 0 or more")
    private Integer annualLeave;

    // Manager can decide whether the employee needs approval
    @NotNull(message = "Approval status must be specified")
    private Boolean isPendingApprovalByManager;
}
