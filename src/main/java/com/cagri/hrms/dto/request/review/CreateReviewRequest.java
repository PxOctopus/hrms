package com.cagri.hrms.dto.request.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateReviewRequest(
        String title,
        @NotBlank String content,
        @Min(1)
        @Max(5)
        Integer rating
) {}
