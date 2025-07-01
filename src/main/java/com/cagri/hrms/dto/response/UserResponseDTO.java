package com.cagri.hrms.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserResponseDTO {
    private final Long id;
    private final String fullName;
    private final String email;
}
