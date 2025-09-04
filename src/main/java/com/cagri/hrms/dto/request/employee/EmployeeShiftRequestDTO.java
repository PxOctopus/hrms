package com.cagri.hrms.dto.request.employee;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * Write model for assigning a Shift to an Employee on a single date.
 * Range assignment should be handled by the client or a dedicated bulk endpoint.
 */
@Data
public class EmployeeShiftRequestDTO {

    @NotNull
    private Long employeeId;   // target employee

    @NotNull
    private Long shiftId;      // chosen shift definition

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate shiftDate; // specific calendar day

    // soft delete / status flag; defaults can be applied in service layer
    private Boolean active;
}
