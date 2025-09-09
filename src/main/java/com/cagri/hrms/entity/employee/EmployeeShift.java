package com.cagri.hrms.entity.employee;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(
        name = "employee_shifts"
        // IMPORTANT: Do NOT keep a JPA-level unique constraint here when using soft delete,
        // because old rows still exist. We enforce "one per day" in service level
        // and optionally via a DB partial unique index in production.
        // If you already created a DB-side unique constraint, our service will "reactivate"
        // the existing row instead of inserting a new one, so you are still safe.
        // uniqueConstraints = { @UniqueConstraint(name = "uq_emp_date", columnNames = {"employee_id", "shift_date"}) }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// NOTE: We do NOT use @Where(active = true) because we need to sometimes
// reach inactive rows (e.g., reactivate). We filter "active" in queries explicitly.
@SQLDelete(sql = "UPDATE employee_shifts SET active = false WHERE id = ?") // soft delete hook
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
    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    @Column(nullable = false)
    private boolean active = true; // soft delete flag

    // Breaks related to this employee shift
    @OneToMany(mappedBy = "employeeShift", cascade = CascadeType.ALL)
    private List<Break> breaks;

    // TIP: If you ever need audit fields later (deletedAt/deletedBy),
    // you can add nullable columns without touching the behavior now.
}
