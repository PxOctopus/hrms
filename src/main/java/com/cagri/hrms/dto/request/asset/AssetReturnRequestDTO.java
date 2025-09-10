package com.cagri.hrms.dto.request.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetReturnRequestDTO {
    // Employee requests return
    private String reason; // event metadata
}
