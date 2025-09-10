package com.cagri.hrms.dto.request.asset;

import com.cagri.hrms.enums.AssetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetChangeStatusRequestDTO {
    // Manager changes status (e.g., MAINTENANCE, IN_STOCK, RETIRED)
    private AssetStatus status;
    private String note;
}
