package com.cagri.hrms.dto.request.expense;

import com.cagri.hrms.enums.expense.ExpenseCategory;
import com.cagri.hrms.enums.expense.PaymentMethod;
import com.cagri.hrms.enums.expense.ReceiptType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Update payload — partial update (PATCH-like).
 * Null fields are ignored by mapper (NullValuePropertyMappingStrategy.IGNORE).
 */
@Data
public class ExpenseUpdateDTO {

    private ExpenseCategory category;

    /** Optional — if null, keep current value. */
    private PaymentMethod paymentMethod;

    /** Optional — if null, keep or service may default. */
    private ReceiptType receiptType;

    private LocalDate expenseDate;

    @Size(max = 8)
    private String currency;

    @DecimalMin("0.01")
    private BigDecimal grossAmount;

    @DecimalMin("0.00")
    private BigDecimal vatAmount;

    @DecimalMin("0.00")
    private BigDecimal tipAmount;

    @Size(max = 2000)
    private String note;

    private String location;

    private List<String> receiptFiles;
}
