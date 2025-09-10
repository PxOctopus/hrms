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
public class AssetUpdateRequestDTO {
    // Manager can update basic fields; status changes use a separate endpoint
    private String assetName;
    private String category;
    private String description;
    private AssetCondition condition;
    private String location;
}
