package com.cagri.hrms.dto.response.expense;

import com.cagri.hrms.enums.expense.PayrollAdjustmentType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayrollAdjustmentDTO {
    private Long id;

    private Long employeeId;                 // entity.employee.id
    private PayrollAdjustmentType type;      // REIMBURSEMENT (vs. future types)
    private BigDecimal amount;               // payroll currency
    private String currency;                 // original currency (optional)
    private String description;

    private LocalDate effectiveDate;         // when it will be/was included in payroll
    private boolean processed;               // only true
    private Long expenseId;                  // source expense (nullable)
    private LocalDateTime createdAt;
}
