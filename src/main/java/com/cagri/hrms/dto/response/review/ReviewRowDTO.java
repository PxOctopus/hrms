package com.cagri.hrms.dto.response.review;

import com.cagri.hrms.enums.ReviewStatus;

import java.time.Instant;

public record ReviewRowDTO(
        Long id, String title, String content, Integer rating,
        String managerName, ReviewStatus status, Instant createdAt, String rejectionReason
) {}
