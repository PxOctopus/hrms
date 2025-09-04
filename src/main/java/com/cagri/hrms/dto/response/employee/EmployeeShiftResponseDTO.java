package com.cagri.hrms.dto.response.employee;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * Read model for an employee's shift assignment on a specific date.
 * Returned by list/read endpoints (never used to create/update).
 */
@Data
public class EmployeeShiftResponseDTO {
    private Long id;

    // Denormalized employee info for convenience in the UI
    private Long employeeId;
    private String employeeName;

    // Denormalized shift info for convenience in the UI
    private Long shiftId;
    private String shiftName;

    // Keep a stable wire format for dates
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate shiftDate;

    private boolean active;
}