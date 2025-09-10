package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.asset.*;
import com.cagri.hrms.dto.response.asset.AssetEventResponseDTO;
import com.cagri.hrms.dto.response.asset.AssetMaintenanceResponseDTO;
import com.cagri.hrms.dto.response.asset.AssetResponseDTO;          // FULL DTO (manager/admin views)
import com.cagri.hrms.dto.response.employee.EmployeeAssetResponseDTO; // SLIM DTO (employee self-view)
import com.cagri.hrms.enums.AssetStatus;

import java.util.List;

public interface AssetService {

    // --- Manager flows (return FULL DTO) ---
    AssetResponseDTO create(AssetCreateRequestDTO dto);

    AssetResponseDTO update(Long id, AssetUpdateRequestDTO dto);

    void softDelete(Long id);

    // Company-scoped listing (manager view)
    List<AssetResponseDTO> listByCompany(Long companyId, AssetStatus status);

    // Assign / status transitions (manager actions)
    AssetResponseDTO assign(Long assetId, AssetAssignRequestDTO dto);

    AssetResponseDTO changeStatus(Long assetId, AssetChangeStatusRequestDTO dto);

    // --- Employee self-service ---
    // Employee’s own list → SLIM DTO
    List<EmployeeAssetResponseDTO> listMyAssets(Long employeeId);

    // Employee actions can still return FULL DTO so FE sees status/confirmed/etc.
    AssetResponseDTO confirm(Long assetId, AssetConfirmRequestDTO dto, Long employeeId);

    AssetResponseDTO requestReturn(Long assetId, AssetReturnRequestDTO dto, Long employeeId);

    AssetResponseDTO reportIssue(Long assetId, AssetIssueReportRequestDTO dto, Long employeeId);

    // --- Events & Maintenance ---
    List<AssetEventResponseDTO> events(Long assetId);

    AssetMaintenanceResponseDTO openMaintenance(Long assetId, MaintenanceOpenRequestDTO dto, Long actorUserId);

    AssetMaintenanceResponseDTO closeMaintenance(Long maintenanceId, MaintenanceCloseRequestDTO dto, Long actorUserId);
}
