package com.cagri.hrms.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class UserResponseDTO {
    private final Long id;
    private final String fullName;
    private final String email;
    private String role;
}
