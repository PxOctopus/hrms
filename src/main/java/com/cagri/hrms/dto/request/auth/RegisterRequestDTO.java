package com.cagri.hrms.dto.request.auth;

import com.cagri.hrms.enums.ContractType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "Full name is required")
    private final String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private final String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain at least 1 uppercase letter, 1 lowercase letter, and 1 digit"
    )
    private final String password;


}
