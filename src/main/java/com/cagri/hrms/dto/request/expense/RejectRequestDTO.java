package com.cagri.hrms.dto.request.expense;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectRequestDTO {
    @NotBlank
    private String reason;
}
