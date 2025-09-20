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

/**
 * Expense entity for MVP (no Project field).
 * Design goals:
 *  - Keep persistence model minimal but production-friendly
 *  - Support a lightweight approval workflow
 *  - Be explicit about money/precision and audit fields
 */
@Entity
@Table(
        name = "expenses",
        indexes = {
                // Speeds up "my expenses" queries and company-side reporting
                @Index(name = "ix_expense_employee", columnList = "employee_id"),
                // Speeds up manager review queues filtered by status (SUBMITTED, APPROVED, etc.)
                @Index(name = "ix_expense_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    // --- Identity ---

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The owner/creator of the expense.
    // LAZY to avoid pulling the entire Employee graph in list endpoints.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Employee employee;

    // --- Business fields ---

    // Business classification for reporting and policy (e.g., MEAL, TRAVEL, SUPPLIES)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ExpenseCategory category;

    // Workflow status. Starts at DRAFT and transitions via submit/approve/reject/withdraw helpers.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ExpenseStatus status = ExpenseStatus.DRAFT;

    // Payment characteristics are optional in MVP; can be enforced later by validation rules.
    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 40)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 40)
    private ReceiptType receiptType;

    // Accounting date when the expense happened (NOT createdAt).
    @Column(nullable = false)
    private LocalDate expenseDate;

    // ISO-4217 currency code; using CHAR(3)-like length keeps values consistent ("TRY", "USD"...)
    @Column(nullable = false, length = 3)
    private String currency = "TRY";

    // Use BigDecimal for money; precision/scale prevent floating errors and cap extreme values.
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal grossAmount;

    // Optional components; not all expenses have VAT/TIP.
    @Column(precision = 18, scale = 2)
    private BigDecimal vatAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal tipAmount;

    // Optional computed field. Service layer is free to populate it from gross+vat+tip (or a custom rule).
    // Keeping it denormalized simplifies reporting without re-computation on every read.
    @Column(precision = 18, scale = 2)
    private BigDecimal netAmount;

    // Optional metadata that helps users/managers contextualize the expense.
    private String location;

    @Column(length = 2000)
    private String note;

    // Lightweight file storage model:
    // We store only keys/paths (e.g., S3 object keys) to avoid binary blobs in DB.
    // ElementCollection is fine here because the child table is simple (expense_id, file_key).
    @ElementCollection
    @CollectionTable(name = "expense_files", joinColumns = @JoinColumn(name = "expense_id"))
    @Column(name = "file_key", length = 400)
    private List<String> receiptFiles = new ArrayList<>();

    // --- Workflow audit ---

    // When the employee sent the expense for manager review.
    private LocalDateTime submittedAt;

    // When a manager approved or rejected the expense.
    private LocalDateTime managerReviewedAt;

    // Who reviewed it (store the reviewer user id; avoids a hard FK to keep the MVP simple).
    private Long managerReviewerId;

    // Free-text note for the decision (e.g., "Approved. Receipt verified." / "Rejected: missing receipt").
    @Column(length = 1000)
    private String managerDecisionNote;

    // --- Payment tracking (post-payroll) ---

    // When the reimbursement was actually paid to the employee.
    private LocalDateTime paidAt;

    // Who executed the payment in finance (keep as id for loose coupling).
//    private Long financeUserId;

    // --- Persistence timestamps ---
    // These are general record timestamps (different from business dates like expenseDate).
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // --- Domain helpers (explicit, testable transitions) ---
    // Why have these methods? So services/controllers
    // 1) don't re-implement status rules everywhere,
    // 2) keep transitions consistent and auditable,
    // 3) are easy to unit test without hitting the DB.

    /** Move from DRAFT to SUBMITTED; sets submittedAt. */
    public void submit() {
        this.status = ExpenseStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    /** Approve the expense; stamps reviewer/time and optional note. */
    public void approve(Long reviewerId, String note) {
        this.status = ExpenseStatus.APPROVED;
        this.managerReviewedAt = LocalDateTime.now();
        this.managerReviewerId = reviewerId;
        this.managerDecisionNote = note;
    }

    /** Reject the expense; stamps reviewer/time and reason. */
    public void reject(Long reviewerId, String note) {
        this.status = ExpenseStatus.REJECTED;
        this.managerReviewedAt = LocalDateTime.now();
        this.managerReviewerId = reviewerId;
        this.managerDecisionNote = note;
    }

    /** Allow employee to cancel while still not paid; MVP keeps rule simple. */
    public void withdraw() {
        this.status = ExpenseStatus.WITHDRAWN;
    }

    // --- Lifecycle hooks ---
    // Why @PrePersist/@PreUpdate?
    // - Central, reliable timestamps without duplicating code in every service.
    // - Safe even if the entity is saved from multiple places.
    // - Keeps createdAt/updatedAt in sync with the DB lifecycle.

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
