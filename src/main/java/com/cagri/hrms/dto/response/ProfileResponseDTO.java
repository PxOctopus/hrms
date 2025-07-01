package com.cagri.hrms.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProfileResponseDTO {
    private final String fullName;
    private final String email;
}
