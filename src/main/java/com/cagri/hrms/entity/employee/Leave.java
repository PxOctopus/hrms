package com.cagri.hrms.entity.employee;

import com.cagri.hrms.entity.core.LeaveDefinition;
import com.cagri.hrms.enums.LeaveStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "leaves")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reason;

    private LocalDate startDate;
    private LocalDate endDate;

    private LocalDate requestDate;     // Date when the leave was requested
    private LocalDate decisionDate;    // Date when the leave was approved or rejected
    private String managerNote;        // Optional note by the approving/rejecting manager

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;        // Leave status: PENDING, APPROVED, or REJECTED

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;         // Employee who requested the leave

    @ManyToOne
    @JoinColumn(name = "leave_definition_id", nullable = false)
    private LeaveDefinition leaveDefinition;  // Type of leave (Annual, Sick, etc.)
}
