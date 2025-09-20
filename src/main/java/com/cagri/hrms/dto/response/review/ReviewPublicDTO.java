package com.cagri.hrms.dto.response.review;

import java.time.Instant;

public record ReviewPublicDTO(
        Long id, String managerName, String content, Integer rating, Instant createdAt
) {}
