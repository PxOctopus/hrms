package com.cagri.hrms.dto.response.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
public class AuthResponseDTO {
    private final String accessToken;
    private final String role;
    private final String tokenType = "Bearer";
    private boolean mustChangePassword;

    public AuthResponseDTO(String accessToken, String role, boolean mustChangePassword) {
        this.accessToken = accessToken;
        this.role = role;
        this.mustChangePassword = mustChangePassword;
    }
}
