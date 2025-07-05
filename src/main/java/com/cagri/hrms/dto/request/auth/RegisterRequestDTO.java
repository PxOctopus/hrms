package com.cagri.hrms.dto.request.auth;

import com.cagri.hrms.enums.ContractType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain at least 1 uppercase letter, 1 lowercase letter, and 1 digit"
    )
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Company email is required")
    @Email(message = "Please enter a valid company email")
    private String companyEmail;

    @AssertTrue(message = "Passwords do not match")
    @Schema(hidden = true)
    public boolean isPasswordMatching() {
        if (password == null || confirmPassword == null) return false;
        return password.equals(confirmPassword);
    }

    @Pattern(regexp = "^(EMPLOYEE|MANAGER|ADMIN)$", message = "Invalid role")
    private String roleName;
}
