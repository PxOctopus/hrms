package com.cagri.hrms.entity.employee;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "breaks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Break {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "break_name", nullable = false)
    private String breakName;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    // The employee who owns this break
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // The employee shift this break belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_shift_id", nullable = false)
    private EmployeeShift employeeShift;

    @Column(nullable = false)
    private boolean active = true; // For soft delete or status tracking
}
