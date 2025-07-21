package com.cagri.hrms.dto.response.employee;

import lombok.Data;

import java.time.LocalTime;

@Data
public class ShiftResponseDTO {
    private Long id;
    private String shiftName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long companyId;
}
