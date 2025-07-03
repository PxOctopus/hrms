package com.cagri.hrms.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    private final String email;

    @NotBlank(message = "Password is required")
    private final String password;
}
