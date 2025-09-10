package com.cagri.hrms.dto.request.asset;

import com.cagri.hrms.enums.AssetCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetCreateRequestDTO {
    // Manager creates new inventory item (IN_STOCK by default)
    // Validation rules are minimal for MVP and can be extended.
    private String assetName;
    private String serialNumber;
    private String category;
    private String description;
    private AssetCondition condition; // optional; default NEW if null
    private String location;
}
