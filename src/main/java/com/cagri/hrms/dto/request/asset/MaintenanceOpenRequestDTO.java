package com.cagri.hrms.dto.request.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceOpenRequestDTO {
    // Employee or Manager can open maintenance
    private String vendorName;
    private String notes;
}
