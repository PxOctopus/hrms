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

    void softDelete(Long id); // archive (active=false)

    // Company-scoped listing (manager view)
    List<AssetResponseDTO> listByCompany(Long companyId, AssetStatus status);

    // Assign / status transitions (manager actions)
    AssetResponseDTO assign(Long assetId, AssetAssignRequestDTO dto);

    AssetResponseDTO changeStatus(Long assetId, AssetChangeStatusRequestDTO dto);
    // Note: Manager approves retirement by calling changeStatus with
    // old=RETIRE_REQUESTED and new=RETIRED.

    // --- Employee self-service ---
    List<EmployeeAssetResponseDTO> listMyAssets(Long employeeId);

    AssetResponseDTO confirm(Long assetId, AssetConfirmRequestDTO dto, Long employeeId);

    AssetResponseDTO requestReturn(Long assetId, AssetReturnRequestDTO dto, Long employeeId);

    AssetResponseDTO reportIssue(Long assetId, AssetIssueReportRequestDTO dto, Long employeeId);
    // Note: For retirement ask, employee sends issueType=RETIRE_REQUESTED (not RETIRED).

    // --- Undo actions (employee) ---
    AssetResponseDTO cancelReturnRequest(Long assetId, Long employeeId);

    AssetResponseDTO cancelIssueReport(Long assetId, Long employeeId);

    // --- Events & Maintenance ---
    List<AssetEventResponseDTO> events(Long assetId);

    AssetMaintenanceResponseDTO openMaintenance(Long assetId, MaintenanceOpenRequestDTO dto, Long actorUserId);

    AssetMaintenanceResponseDTO closeMaintenance(Long maintenanceId, MaintenanceCloseRequestDTO dto, Long actorUserId);
}
