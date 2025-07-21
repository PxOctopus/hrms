package com.cagri.hrms.dto.response.employee;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExpenseResponseDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String description;
    private BigDecimal amount;
    private LocalDateTime expenseDate;
    private String fileUrl;
    private String status;
    private String managerNote;
    private boolean active;
}
