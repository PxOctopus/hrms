package com.cagri.hrms.dto.request.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceCloseRequestDTO {
    // Manager closes maintenance
    private String notes; // final notes
    private boolean restoreToAssigned; // if true and asset had employee, go back to ASSIGNED_CONFIRMED
}
