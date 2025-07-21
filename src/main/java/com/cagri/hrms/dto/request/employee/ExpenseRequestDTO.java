package com.cagri.hrms.dto.request.employee;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExpenseRequestDTO {
    private Long employeeId;
    private String description;
    private BigDecimal amount;
    private LocalDateTime expenseDate;
    private String fileUrl; // To upload a file
}