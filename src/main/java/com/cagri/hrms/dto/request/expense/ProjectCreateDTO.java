package com.cagri.hrms.dto.request.expense;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProjectCreateDTO {
    @NotBlank
    private String name;
    private String code;
    private String description;
    private boolean requiresAssignment = false;
    private boolean generic = false; // company-wide flag
    private BigDecimal budgetLimit;
    private BigDecimal monthlyCap;
}
