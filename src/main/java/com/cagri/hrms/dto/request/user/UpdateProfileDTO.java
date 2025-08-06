package com.cagri.hrms.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Reserved for future use — possible admin-only fullName update.
 * Not currently used in any endpoint.
 */
@Getter
@RequiredArgsConstructor
public class UpdateProfileDTO {

    @NotBlank(message = "Full name is required")
    private final String fullName;
}
