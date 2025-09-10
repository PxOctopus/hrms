package com.cagri.hrms.dto.request.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetIssueReportRequestDTO {
    // Employee reports issue or loss
    private String issueType;    // e.g., "DAMAGE" or "LOSS"
    private String description;  // details
}
