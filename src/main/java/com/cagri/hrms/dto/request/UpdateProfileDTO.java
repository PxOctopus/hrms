package com.cagri.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateProfileDTO {

    @NotBlank(message = "Full name is required")
    private final String fullName;
}
