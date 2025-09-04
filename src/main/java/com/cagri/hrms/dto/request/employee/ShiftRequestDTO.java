package com.cagri.hrms.dto.request.employee;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

/**
 * Write model for creating/updating a Shift.
 * Note:
 *  - Company should be derived from the authenticated user on the server (ignore companyId on write).
 *  - Overnight shifts ARE allowed; validation that depends on business rules is handled in the service layer.
 */
@Data
public class ShiftRequestDTO {

    @NotBlank
    private String shiftName;

    @NotNull
    @JsonFormat(pattern = "HH:mm") // keep a stable wire format: "08:00"
    private LocalTime startTime;

    @NotNull
    @JsonFormat(pattern = "HH:mm") // keep a stable wire format: "16:00"
    private LocalTime endTime;

    /**
     * Optional field; recommended to ignore in the mapper and set company from auth.
     * Keep only if you must support admin-level cross-company actions.
     */
    private Long companyId;
}
