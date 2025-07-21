package com.cagri.hrms.dto.request.employee;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeShiftRequestDTO {
    private Long employeeId;      // ID of the employee to assign the shift to
    private Long shiftId;         // ID of the shift definition
    private LocalDate shiftDate;  // Date for the shift assignment
    private Boolean active;
}
