package com.cagri.hrms.dto.response.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuthResponseDTO {
    private final String accessToken;
    private final String role;
    private final String tokenType = "Bearer";
}
