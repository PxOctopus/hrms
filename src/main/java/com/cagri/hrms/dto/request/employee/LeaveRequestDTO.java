package com.cagri.hrms.dto.request.employee;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveRequestDTO {
    private String reason;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long employeeId;
    private Long leaveDefinitionId;
}
