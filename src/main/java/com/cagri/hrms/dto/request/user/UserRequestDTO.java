package com.cagri.hrms.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDTO {

    @NotBlank(message = "Full name must not be blank")
    private String fullName;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    private String password;

    // If the admin manually assigns the user role
    private String roleName;
}
