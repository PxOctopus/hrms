package com.cagri.hrms.dto.response.employee;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Lightweight projection used by weekly range endpoints.
 * Contains denormalized shift times to avoid N+1 on the client.
 */
@Data
@AllArgsConstructor
public class EmployeeShiftWeekItemDTO {
    private Long id;
    private Long shiftId;
    private String shiftName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate shiftDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
}
