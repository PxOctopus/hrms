package com.cagri.hrms.dto.response.employee;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeShiftResponseDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long shiftId;
    private String shiftName;
    private LocalDate shiftDate;
    private boolean active;
}