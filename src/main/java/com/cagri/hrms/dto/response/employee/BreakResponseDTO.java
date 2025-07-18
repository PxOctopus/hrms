package com.cagri.hrms.dto.response.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreakResponseDTO {

    private Long id;
    private String breakName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long employeeId;
    private String employeeFullName;
}
