package com.cagri.hrms.dto.request.employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Lightweight request for live overlap/quota checks. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCheckDTO {
    private Long employeeId;         // optional; null => resolve from current user
    private Long leaveDefinitionId;  // required
    private LocalDate startDate;     // required
    private LocalDate endDate;       // required
}
