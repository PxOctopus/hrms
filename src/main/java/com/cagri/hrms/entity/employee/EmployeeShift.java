package com.cagri.hrms.entity.employee;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "employee_shifts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The employee assigned to this shift
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // The shift definition (e.g., night, day)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    // Date when this shift is assigned to the employee
    @Column(nullable = false)
    private LocalDate shiftDate;

    @Column(nullable = false)
    private boolean active = true; // Soft delete/status flag

    //  Breaks related to this employee shift
    @OneToMany(mappedBy = "employeeShift", cascade = CascadeType.ALL)
    private List<Break> breaks;
}


