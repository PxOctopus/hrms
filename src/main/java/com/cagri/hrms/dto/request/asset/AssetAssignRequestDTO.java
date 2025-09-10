package com.cagri.hrms.dto.request.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetAssignRequestDTO {
    // Manager assigns asset to employee
    private Long employeeId;
    private String note; // will be written to event metadata
}
