package com.cagri.hrms.entity.expense;

import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.enums.expense.PayrollAdjustmentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="payroll_adjustments",
        indexes = @Index(name="ix_payroll_employee_processed", columnList="employee_id,processed"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollAdjustment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PayrollAdjustmentType type = PayrollAdjustmentType.REIMBURSEMENT;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount; // payroll currency (TRY)

    @Column(length = 8)
    private String currency;   // original currency if different

    @Column(length = 300)
    private String description;

    @Column(nullable = false)
    private LocalDate effectiveDate; // when to include in payroll

    private boolean processed = false; // set true by payroll job

    // Trace back to the source expense (nullable but recommended)
    private Long expenseId;

    private LocalDateTime createdAt;

    @PrePersist void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
