package com.cagri.hrms.dto.request.employee;

import lombok.Data;

import java.time.LocalTime;

@Data
public class ShiftRequestDTO {
    private String shiftName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long companyId;
}
