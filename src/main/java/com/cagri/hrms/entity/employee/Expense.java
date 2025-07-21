package com.cagri.hrms.entity.employee;

import com.cagri.hrms.enums.ExpenseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The employee who made the expense
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // Description or title of the expense
    @Column(nullable = false)
    private String description;

    // Expense amount
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // Date when the expense was made
    @Column(nullable = false)
    private LocalDateTime expenseDate;

    // File URL (PDF, image, etc.)
    private String fileUrl;

    // Status of the expense (e.g., PENDING, APPROVED, REJECTED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseStatus status;

    // Manager note
    private String managerNote;

    // Soft delete/status tracking
    @Column(nullable = false)
    private boolean active = true;
}

