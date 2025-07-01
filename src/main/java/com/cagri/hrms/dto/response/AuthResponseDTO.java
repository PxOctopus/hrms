package com.cagri.hrms.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuthResponseDTO {
    private final String accessToken;
    private final String tokenType = "Bearer";
}
