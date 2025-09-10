package com.cagri.hrms.dto.response.asset;

import com.cagri.hrms.enums.AssetCondition;
import com.cagri.hrms.enums.AssetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetResponseDTO {
    private Long id;
    private String assetName;
    private String serialNumber;
    private String category;
    private String description;
    private AssetStatus status;
    private AssetCondition condition;
    private Long employeeId;     // null if IN_STOCK, etc.
    private String employeeName; // optional for convenience
    private Long managerId;
    private Long companyId;
    private LocalDate assignedDate;
    private boolean confirmed;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
