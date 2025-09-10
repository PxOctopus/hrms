package com.cagri.hrms.dto.response.asset;

import com.cagri.hrms.entity.asset.AssetEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetEventResponseDTO {
    private Long id;
    private Long assetId;
    private AssetEvent.EventType type;
    private Long actorUserId;
    private String metadataJson;
    private LocalDateTime createdAt;
}
