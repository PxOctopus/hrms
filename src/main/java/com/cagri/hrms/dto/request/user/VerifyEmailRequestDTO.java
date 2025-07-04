package com.cagri.hrms.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifyEmailRequestDTO {
    @NotBlank(message = "Verification token is required")
    private String token;
}
