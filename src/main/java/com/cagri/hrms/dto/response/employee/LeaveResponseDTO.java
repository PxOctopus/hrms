package com.cagri.hrms.dto.response.employee;

import com.cagri.hrms.enums.LeaveStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveResponseDTO {
    private Long id;
    private String reason;
    private LocalDate startDate;
    private LocalDate endDate;

    private LeaveStatus status;        // Current status of the leave request
    private Long employeeId;           // ID of the employee
    private String employeeFullName;   // Full name of the employee
    private String leaveDefinitionName;// Human-readable name of the leave type

    private LocalDate requestDate;     // Date when the leave was requested
    private LocalDate decisionDate;    // Date of manager’s decision
    private String managerNote;        // Optional note provided by the manager
}
