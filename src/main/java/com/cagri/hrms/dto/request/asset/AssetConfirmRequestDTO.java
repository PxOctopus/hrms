package com.cagri.hrms.dto.request.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetConfirmRequestDTO {
    // Employee confirms receiving the asset
    private String note; // optional, goes to event metadata
}
