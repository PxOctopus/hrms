package com.cagri.hrms.dto.response.general;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponseDTO {
    // The unique code representing the error type (e.g. "VALIDATION_ERROR")
    private String code;

    // A human-readable message describing the error
    private String message;
}
