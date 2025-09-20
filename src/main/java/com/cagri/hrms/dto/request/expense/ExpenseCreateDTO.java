package com.cagri.hrms.dto.request.expense;

import com.cagri.hrms.enums.expense.ExpenseCategory;
import com.cagri.hrms.enums.expense.PaymentMethod;
import com.cagri.hrms.enums.expense.ReceiptType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Create payload — project removed. Keep MVP minimal; some fields optional.
 */
@Data
public class ExpenseCreateDTO {

    @NotNull(message = "category is required")
    private ExpenseCategory category;

    /** Optional in MVP; make it @NotNull if you want to enforce. */
    private PaymentMethod paymentMethod;

    /** Optional in MVP; service can default to ReceiptType.RECEIPT if null. */
    private ReceiptType receiptType;

    @NotNull(message = "expenseDate is required")
    private LocalDate expenseDate;

    /** ISO-4217 currency code; default "TRY". */
    @NotBlank
    @Size(max = 8) // keep 8 if backend entity allows 8; typically 3 is enough for ISO code.
    private String currency = "TRY";

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal grossAmount;

    @DecimalMin("0.00")
    private BigDecimal vatAmount;

    @DecimalMin("0.00")
    private BigDecimal tipAmount;

    @Size(max = 2000)
    private String note;

    private String location;

    /** Storage keys/paths for uploaded receipts (optional). */
    private List<String> receiptFiles;
}
