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

@Data
public class ExpenseCreateDTO {
    // Optional; service will default to company-wide if null
    private Long projectId;

    @NotNull
    private ExpenseCategory category;
    @NotNull private PaymentMethod paymentMethod;
    @NotNull private ReceiptType receiptType;

    @NotNull private LocalDate expenseDate;

    @NotBlank
    @Size(max = 8)
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

    // File keys uploaded via separate endpoint; may be set later
    private List<String> receiptFiles;
}
