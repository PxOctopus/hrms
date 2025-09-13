package com.cagri.hrms.dto.response.expense;

import com.cagri.hrms.enums.expense.ExpenseCategory;
import com.cagri.hrms.enums.expense.ExpenseStatus;
import com.cagri.hrms.enums.expense.PaymentMethod;
import com.cagri.hrms.enums.expense.ReceiptType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponseDTO {
    private Long id;

    private ExpenseStatus status;
    private ExpenseCategory category;

    private String currency;
    private BigDecimal grossAmount;
    private BigDecimal vatAmount;
    private BigDecimal tipAmount;
    private BigDecimal netAmount;

    private PaymentMethod paymentMethod;
    private ReceiptType receiptType;

    private LocalDate expenseDate;
    private String location;
    private String note;

    private Long projectId;
    private String projectCode;
    private String projectName;
    private boolean projectGeneric;

    private List<String> receiptFiles;

    // Audit
    private LocalDateTime submittedAt;
    private LocalDateTime managerReviewedAt;
    private Long managerReviewerId;
    private String managerDecisionNote;
    private LocalDateTime paidAt;

    // UI hints
    private List<String> allowedActions; // e.g., ["EDIT","SUBMIT","DELETE"]
}


