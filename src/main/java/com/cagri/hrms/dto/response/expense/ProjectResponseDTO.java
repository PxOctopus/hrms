package com.cagri.hrms.dto.response.expense;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProjectResponseDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private boolean active;
    private boolean generic;
    private boolean requiresAssignment;
    private BigDecimal budgetLimit;
    private BigDecimal monthlyCap;
}
