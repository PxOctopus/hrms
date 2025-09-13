package com.cagri.hrms.entity.expense;

import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.enums.expense.ExpenseCategory;
import com.cagri.hrms.enums.expense.ExpenseStatus;
import com.cagri.hrms.enums.expense.PaymentMethod;
import com.cagri.hrms.enums.expense.ReceiptType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="expenses",
        indexes = {
                @Index(name="ix_expense_employee", columnList="employee_id"),
                @Index(name="ix_expense_status", columnList="status"),
                @Index(name="ix_expense_project", columnList="project_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    private Project project; // nullable; service will set company-wide if null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ExpenseCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ExpenseStatus status = ExpenseStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReceiptType receiptType;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @Column(nullable = false, length = 8)
    private String currency = "TRY";

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal grossAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal vatAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal tipAmount;

    // Optional computed field; set by service to keep reporting simple
    @Column(precision = 18, scale = 2)
    private BigDecimal netAmount;

    private String location;

    @Column(length = 2000)
    private String note;

    // Store only file keys/paths (S3/local); use a separate storage service
    @ElementCollection
    @CollectionTable(name = "expense_files", joinColumns = @JoinColumn(name="expense_id"))
    @Column(name="file_key", length = 400)
    private List<String> receiptFiles = new ArrayList<>();

    // Workflow audit
    private LocalDateTime submittedAt;
    private LocalDateTime managerReviewedAt;
    private Long managerReviewerId;
    @Column(length = 1000)
    private String managerDecisionNote;

    // Paid tracking (derived from payroll processing)
    private LocalDateTime paidAt;
    private Long financeUserId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }
    @PreUpdate void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
