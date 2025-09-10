package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.asset.*;
import com.cagri.hrms.dto.request.employee.AssetRequestDTO;
import com.cagri.hrms.dto.response.asset.AssetEventResponseDTO;
import com.cagri.hrms.dto.response.asset.AssetMaintenanceResponseDTO;
import com.cagri.hrms.dto.response.employee.AssetResponseDTO;
import com.cagri.hrms.enums.AssetStatus;

import java.util.List;

public interface AssetService {

    AssetResponseDTO create(AssetCreateRequestDTO dto);

    AssetResponseDTO update(Long id, AssetUpdateRequestDTO dto);

    void softDelete(Long id); // optional

    List<AssetResponseDTO> listByCompany(Long companyId, AssetStatus status);

    List<AssetResponseDTO> listMyAssets(Long employeeId);

    AssetResponseDTO assign(Long assetId, AssetAssignRequestDTO dto);

    AssetResponseDTO changeStatus(Long assetId, AssetChangeStatusRequestDTO dto);

    AssetResponseDTO confirm(Long assetId, AssetConfirmRequestDTO dto, Long employeeId);

    AssetResponseDTO requestReturn(Long assetId, AssetReturnRequestDTO dto, Long employeeId);

    AssetResponseDTO reportIssue(Long assetId, AssetIssueReportRequestDTO dto, Long employeeId);

    List<AssetEventResponseDTO> events(Long assetId);

    // Maintenance
    AssetMaintenanceResponseDTO openMaintenance(Long assetId, MaintenanceOpenRequestDTO dto, Long actorUserId);
    AssetMaintenanceResponseDTO closeMaintenance(Long maintenanceId, MaintenanceCloseRequestDTO dto, Long actorUserId);
}
