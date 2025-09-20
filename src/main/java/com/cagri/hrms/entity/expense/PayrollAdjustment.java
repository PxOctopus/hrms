package com.cagri.hrms.entity.expense;

import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.enums.expense.PayrollAdjustmentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PayrollAdjustment
 * - Represents a payroll-side adjustment (e.g., reimbursement for an approved expense).
 * - Minimal but production-friendly: amount, when to apply (effectiveDate), processed flag.
 * - Links back to the source expense via expenseId (soft link to keep coupling low).
 */
@Entity
@Table(
        name="payroll_adjustments",
        indexes = {
                @Index(name="ix_payroll_employee_processed", columnList="employee_id,processed")
        },
        uniqueConstraints = {
                @UniqueConstraint(name="uk_payroll_expense", columnNames = "expense_id")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PayrollAdjustment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PayrollAdjustmentType type = PayrollAdjustmentType.REIMBURSEMENT;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(length = 8)
    private String currency;

    @Column(length = 300)
    private String description;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    private boolean processed = false;

    @Column(name="expense_id", nullable = false)
    private Long expenseId;

    private LocalDateTime createdAt;

    @PrePersist void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
