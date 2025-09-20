package com.cagri.hrms.dto.response.expense;

import com.cagri.hrms.enums.expense.ExpenseCategory;
import com.cagri.hrms.enums.expense.ExpenseStatus;
import com.cagri.hrms.enums.expense.PaymentMethod;
import com.cagri.hrms.enums.expense.ReceiptType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true) // handy if you ever want to copy & tweak in service
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // omit nulls in JSON
public class ExpenseResponseDTO {
    private Long id;

    // Status & categorization
    private ExpenseStatus status;
    private ExpenseCategory category;

    // Money
    private String currency;              // e.g., "TRY"
    private BigDecimal grossAmount;
    private BigDecimal vatAmount;
    private BigDecimal tipAmount;
    private BigDecimal netAmount;

    // Optional payment/receipt metadata
    private PaymentMethod paymentMethod;
    private ReceiptType receiptType;

    // Business date & context
    private LocalDate expenseDate;
    private String location;
    private String note;

    // Files (storage keys/urls)
    private List<String> receiptFiles;

    // Audit
    private LocalDateTime submittedAt;
    private LocalDateTime managerReviewedAt;
    private Long managerReviewerId;
    private String managerDecisionNote;
    private LocalDateTime paidAt;
//    private Long financeUserId; // when financer is available; later show who executed payment

    // UI hints
    private List<String> allowedActions; // e.g., ["EDIT","SUBMIT","DELETE"]

    // employee
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
}
