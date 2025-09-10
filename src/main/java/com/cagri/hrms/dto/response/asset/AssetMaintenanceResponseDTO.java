package com.cagri.hrms.dto.response.asset;

import com.cagri.hrms.entity.asset.AssetMaintenance;
import com.cagri.hrms.enums.MaintenanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetMaintenanceResponseDTO {
    private Long id;
    private Long assetId;
    private String vendorName;
    private String ticketNumber;
    private MaintenanceStatus status;
    private String notes;
    private LocalDate openedDate;
    private LocalDate closedDate;
}
