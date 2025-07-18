package com.cagri.hrms.dto.request.employee;

import lombok.Data;

import java.time.LocalTime;

@Data
public class BreakRequestDTO {
    private String breakName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long employeeId;
}
