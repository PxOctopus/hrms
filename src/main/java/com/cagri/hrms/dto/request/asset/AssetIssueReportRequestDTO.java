package com.cagri.hrms.dto.request.asset;

import com.cagri.hrms.enums.AssetStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetIssueReportRequestDTO {
    // CHANGE: We only need the target status coming from the employee.
    // Allowed values on service: MAINTENANCE, LOST, RETIRED.
    @NotNull
    private AssetStatus issueType;
}
