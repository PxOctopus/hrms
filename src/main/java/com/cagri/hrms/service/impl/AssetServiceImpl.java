package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.asset.*;
import com.cagri.hrms.dto.response.asset.AssetEventResponseDTO;
import com.cagri.hrms.dto.response.asset.AssetMaintenanceResponseDTO;
import com.cagri.hrms.dto.response.asset.AssetResponseDTO;                 // FULL DTO
import com.cagri.hrms.dto.response.employee.EmployeeAssetResponseDTO;     // SLIM DTO
import com.cagri.hrms.entity.asset.Asset;
import com.cagri.hrms.entity.asset.AssetEvent;
import com.cagri.hrms.entity.asset.AssetMaintenance;
import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.enums.AssetCondition;
import com.cagri.hrms.enums.AssetStatus;
import com.cagri.hrms.enums.EventType;
import com.cagri.hrms.enums.MaintenanceStatus;
import com.cagri.hrms.mapper.AssetMapper;               // maps Asset -> AssetResponseDTO (FULL)
import com.cagri.hrms.mapper.EmployeeAssetMapper;      // maps Asset -> EmployeeAssetResponseDTO (SLIM)
import com.cagri.hrms.repository.AssetEventRepository;
import com.cagri.hrms.repository.AssetMaintenanceRepository;
import com.cagri.hrms.repository.AssetRepository;
import com.cagri.hrms.service.AssetService;
import com.cagri.hrms.service.CompanyService;
import com.cagri.hrms.service.EmployeeService;
import com.cagri.hrms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepo;
    private final AssetEventRepository eventRepo;
    private final AssetMaintenanceRepository maintenanceRepo;

    private final AssetMapper assetMapper;                     // FULL
    private final EmployeeAssetMapper employeeAssetMapper;     // SLIM

    private final UserService userService;
    private final EmployeeService employeeService;
    private final CompanyService companyService;

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------

    /**
     * Manager can "Confirm Issue" only for MAINTENANCE or LOST,
     * and only if there isn't an ISSUE_CONFIRMED after the latest
     * issue-opening event (MAINTENANCE_OPENED or LOST_REPORTED).
     */
    private boolean isIssueConfirmable(Asset asset) {
        AssetStatus s = asset.getStatus();

        // Only MAINTENANCE or LOST are confirmable
        if (s != AssetStatus.MAINTENANCE && s != AssetStatus.LOST) {
            return false;
        }

        // Find the most recent "issue opening" event (maintenance/lost)
        var issueOpenTypes = java.util.List.of(
                EventType.MAINTENANCE_OPENED,
                EventType.LOST_REPORTED
        );

        var lastOpenOpt = eventRepo.findTopByAssetIdAndTypeInOrderByCreatedAtDesc(
                asset.getId(),
                issueOpenTypes
        );

        // Be permissive for legacy/missing data: if no opening event exists, allow confirmation
        if (lastOpenOpt.isEmpty()) {
            return true;
        }

        var lastOpenAt = lastOpenOpt.get().getCreatedAt(); // Adjust type if your timestamp isn't Instant

        // If there is an ISSUE_CONFIRMED after that opening, do not allow another confirmation
        boolean confirmedAlready = eventRepo.existsByAssetIdAndTypeAndCreatedAtAfter(
                asset.getId(),
                EventType.ISSUE_CONFIRMED,
                lastOpenAt
        );

        return !confirmedAlready;
    }

    /** Employee can undo only MAINTENANCE/LOST if manager has not confirmed. */
    private boolean isIssueUndoableByEmployee(Asset asset) {
        AssetStatus s = asset.getStatus();
        if (!(s == AssetStatus.MAINTENANCE || s == AssetStatus.LOST)) return false;
        return !eventRepo.existsByAssetIdAndType(asset.getId(), EventType.ISSUE_CONFIRMED);
    }

    /** Build FULL DTO with enrichment flags. */
    private AssetResponseDTO toDto(Asset asset) {
        AssetResponseDTO dto = assetMapper.toResponse(asset);
        dto.setIssueConfirmable(isIssueConfirmable(asset));
        return dto;
    }

    // ---------------------------------------------------------
    // Manager flows
    // ---------------------------------------------------------

    @Override
    public AssetResponseDTO create(AssetCreateRequestDTO dto) {
        Company company = companyService.getCurrentCompanyOrThrow();

        Asset entity = assetMapper.toEntity(dto);
        entity.setCompany(company);
        entity.setStatus(AssetStatus.IN_STOCK);
        entity.setActive(true);
        entity.setConfirmed(false);
        if (entity.getCondition() == null) {
            entity.setCondition(AssetCondition.NEW);
        }

        entity = assetRepo.save(entity);
        writeEvent(entity, EventType.ASSET_CREATED, "Created asset");
        return toDto(entity);
    }

    @Override
    public AssetResponseDTO update(Long id, AssetUpdateRequestDTO dto) {
        Asset asset = getActiveByIdAndScope(id);
        assetMapper.updateEntity(asset, dto);
        Asset saved = assetRepo.save(asset);
        writeEvent(saved, EventType.ASSET_UPDATED, "Updated asset fields");
        return toDto(saved);
    }

    /** Soft delete (archive). */
    @Override
    public void softDelete(Long id) {
        Asset asset = getActiveByIdAndScope(id);
        asset.setActive(false);
        assetRepo.save(asset);
        writeEvent(asset, EventType.ASSET_ARCHIVED, "Archived (soft-delete)");
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetResponseDTO> listByCompany(Long companyId, AssetStatus status) {
        List<Asset> list = (status == null)
                ? assetRepo.findByCompanyIdAndActiveTrue(companyId)
                : assetRepo.findByCompanyIdAndStatusAndActiveTrue(companyId, status);

        return list.stream().map(this::toDto).toList();
    }

    @Override
    public AssetResponseDTO assign(Long assetId, AssetAssignRequestDTO dto) {
        Asset asset = getActiveByIdAndScope(assetId);
        guardAssign(asset);

        Employee emp = employeeService.getByIdScoped(dto.getEmployeeId());
        User manager = userService.getCurrentUserOrThrow();

        asset.setEmployee(emp);
        asset.setManager(manager);
        asset.setStatus(AssetStatus.ASSIGNED);
        asset.setAssignedDate(LocalDate.now());
        asset.setConfirmed(false);

        Asset saved = assetRepo.save(asset);
        writeEvent(saved, EventType.ASSIGNED, dto.getNote());
        return toDto(saved);
    }

    @Override
    public AssetResponseDTO changeStatus(Long assetId, AssetChangeStatusRequestDTO dto) {
        Asset asset = getActiveByIdAndScope(assetId);

        AssetStatus oldStatus = asset.getStatus();
        AssetStatus newStatus = dto.getStatus();

        // RETIRED is terminal
        if (oldStatus == AssetStatus.RETIRED && newStatus != AssetStatus.RETIRED) {
            throw new IllegalStateException("Retired assets cannot change state.");
        }

        // Confirm Issue: same-status for MAINTENANCE/LOST -> just audit, do not change status
        if (newStatus == oldStatus && (oldStatus == AssetStatus.MAINTENANCE || oldStatus == AssetStatus.LOST)) {
            writeEvent(asset, EventType.ISSUE_CONFIRMED, dto.getNote() != null ? dto.getNote() : "Issue confirmed");
            Asset persisted = assetRepo.save(asset);
            return toDto(persisted);
        }

        // Retirement approval: RETIRE_REQUESTED -> RETIRED
        if (oldStatus == AssetStatus.RETIRE_REQUESTED && newStatus == AssetStatus.RETIRED) {
            asset.setStatus(AssetStatus.RETIRED);
            Asset retired = assetRepo.save(asset);
            writeEvent(retired, EventType.RETIREMENT_APPROVED, dto.getNote() != null ? dto.getNote() : "Retirement approved");
            return toDto(retired);
        }

        // Mark IN_STOCK from MAINTENANCE / LOST / RETURN_REQUESTED
        if (newStatus == AssetStatus.IN_STOCK) {
            if (!(oldStatus == AssetStatus.MAINTENANCE || oldStatus == AssetStatus.LOST || oldStatus == AssetStatus.RETURN_REQUESTED)) {
                throw new IllegalStateException("Only MAINTENANCE, LOST or RETURN_REQUESTED can be moved to IN_STOCK.");
            }
            asset.setEmployee(null);
            asset.setConfirmed(false);
            asset.setAssignedDate(null);

            asset.setStatus(AssetStatus.IN_STOCK);
            Asset savedStock = assetRepo.save(asset);
            writeEvent(savedStock, EventType.STATUS_CHANGED, dto.getNote() != null ? dto.getNote() : "Marked IN_STOCK");
            return toDto(savedStock);
        }

        // Default manual transition
        asset.setStatus(newStatus);
        Asset saved = assetRepo.save(asset);
        writeEvent(saved, EventType.STATUS_CHANGED, dto.getNote());
        return toDto(saved);
    }

    // ---------------------------------------------------------
    // Employee self-service
    // ---------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeAssetResponseDTO> listMyAssets(Long employeeId) {
        return assetRepo.findByEmployeeIdAndActiveTrue(employeeId)
                .stream()
                .map(a -> {
                    EmployeeAssetResponseDTO dto = employeeAssetMapper.toEmployeeDto(a);
                    // expose undo flag for MAINTENANCE/LOST
                    dto.setIssueUndoable(isIssueUndoableByEmployee(a));
                    return dto;
                })
                .toList();
    }

    @Override
    public AssetResponseDTO confirm(Long assetId, AssetConfirmRequestDTO dto, Long employeeId) {
        Asset asset = getActiveByIdAndScope(assetId);

        if (asset.getEmployee() == null || !asset.getEmployee().getId().equals(employeeId)) {
            throw new IllegalStateException("Not authorized to confirm this asset");
        }
        if (asset.getStatus() != AssetStatus.ASSIGNED) {
            throw new IllegalStateException("Asset is not in ASSIGNED state");
        }

        asset.setConfirmed(true);
        asset.setStatus(AssetStatus.ASSIGNED_CONFIRMED);
        Asset saved = assetRepo.save(asset);
        writeEvent(saved, EventType.CONFIRMED, dto.getNote());
        return toDto(saved);
    }

    @Override
    public AssetResponseDTO requestReturn(Long assetId, AssetReturnRequestDTO dto, Long employeeId) {
        Asset asset = getActiveByIdAndScope(assetId);

        if (asset.getEmployee() == null || !asset.getEmployee().getId().equals(employeeId)) {
            throw new IllegalStateException("Not authorized to request return");
        }
        if (asset.getStatus() != AssetStatus.ASSIGNED && asset.getStatus() != AssetStatus.ASSIGNED_CONFIRMED) {
            throw new IllegalStateException("Return request not allowed in current state");
        }

        asset.setStatus(AssetStatus.RETURN_REQUESTED);
        Asset saved = assetRepo.save(asset);
        writeEvent(saved, EventType.RETURN_REQUESTED, dto.getReason());
        return toDto(saved);
    }

    @Override
    public AssetResponseDTO reportIssue(Long assetId, AssetIssueReportRequestDTO dto, Long employeeId) {
        Asset asset = getActiveByIdAndScope(assetId);

        if (asset.getEmployee() == null || !asset.getEmployee().getId().equals(employeeId)) {
            throw new IllegalStateException("Not authorized to report issue");
        }
        if (asset.getStatus() == AssetStatus.RETIRED) {
            throw new IllegalStateException("Retired assets cannot be modified.");
        }

        AssetStatus target = dto.getIssueType();
        // Allowed: MAINTENANCE, LOST, RETIRE_REQUESTED (NOT RETIRED)
        if (target != AssetStatus.MAINTENANCE && target != AssetStatus.LOST && target != AssetStatus.RETIRE_REQUESTED) {
            throw new IllegalArgumentException("Only MAINTENANCE, LOST or RETIRE_REQUESTED are allowed for issue report.");
        }

        asset.setStatus(target);

        if (target == AssetStatus.MAINTENANCE) {
            writeEvent(asset, EventType.MAINTENANCE_OPENED, "Reported by employee");
        } else if (target == AssetStatus.LOST) {
            writeEvent(asset, EventType.LOST_REPORTED, "Reported by employee");
        } else { // RETIRE_REQUESTED
            writeEvent(asset, EventType.RETIREMENT_REQUESTED, "Requested by employee");
        }

        Asset saved = assetRepo.save(asset);
        return toDto(saved);
    }

    // Undo Return
    @Override
    public AssetResponseDTO cancelReturnRequest(Long assetId, Long employeeId) {
        Asset asset = getActiveByIdAndScope(assetId);

        if (asset.getEmployee() == null || !asset.getEmployee().getId().equals(employeeId)) {
            throw new IllegalStateException("Not authorized to cancel return request.");
        }
        if (asset.getStatus() != AssetStatus.RETURN_REQUESTED) {
            throw new IllegalStateException("No pending return request to cancel.");
        }

        asset.setStatus(asset.isConfirmed() ? AssetStatus.ASSIGNED_CONFIRMED : AssetStatus.ASSIGNED);
        Asset saved = assetRepo.save(asset);

        writeEvent(saved, EventType.RETURN_REQUEST_CANCELED, "Canceled by employee");
        return toDto(saved);
    }

    // Undo Issue (MAINTENANCE/LOST) — not for RETIRE_REQUESTED and not after ISSUE_CONFIRMED
    @Override
    public AssetResponseDTO cancelIssueReport(Long assetId, Long employeeId) {
        Asset asset = getActiveByIdAndScope(assetId);

        if (asset.getEmployee() == null || !asset.getEmployee().getId().equals(employeeId)) {
            throw new IllegalStateException("Not authorized to cancel issue.");
        }
        if (asset.getStatus() == AssetStatus.RETIRED) {
            throw new IllegalStateException("Retired assets cannot be undone.");
        }
        if (!(asset.getStatus() == AssetStatus.MAINTENANCE || asset.getStatus() == AssetStatus.LOST)) {
            throw new IllegalStateException("Current state is not undoable.");
        }
        if (eventRepo.existsByAssetIdAndType(asset.getId(), EventType.ISSUE_CONFIRMED)) {
            throw new IllegalStateException("Issue already confirmed by manager.");
        }

        asset.setStatus(asset.isConfirmed() ? AssetStatus.ASSIGNED_CONFIRMED : AssetStatus.ASSIGNED);
        Asset saved = assetRepo.save(asset);

        writeEvent(saved, EventType.ISSUE_REPORT_CANCELED, "Canceled by employee");
        return toDto(saved);
    }

    // ---------------------------------------------------------
    // Events & Maintenance
    // ---------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<AssetEventResponseDTO> events(Long assetId) {
        return eventRepo.findByAssetIdOrderByCreatedAtAsc(assetId)
                .stream()
                .map(assetMapper::toResponse)
                .toList();
    }

    @Override
    public AssetMaintenanceResponseDTO openMaintenance(Long assetId, MaintenanceOpenRequestDTO dto, Long actorUserId) {
        Asset asset = getActiveByIdAndScope(assetId);
        asset.setStatus(AssetStatus.MAINTENANCE);

        AssetMaintenance m = AssetMaintenance.builder()
                .asset(asset)
                .requestedBy(userService.getById(actorUserId))
                .vendorName(dto.getVendorName())
                .status(MaintenanceStatus.OPEN)
                .notes(dto.getNotes())
                .openedDate(LocalDate.now())
                .build();

        assetRepo.save(asset);
        maintenanceRepo.save(m);

        writeEvent(asset, EventType.MAINTENANCE_OPENED, dto.getNotes());
        return assetMapper.toResponse(m);
    }

    @Override
    public AssetMaintenanceResponseDTO closeMaintenance(Long maintenanceId, MaintenanceCloseRequestDTO dto, Long actorUserId) {
        AssetMaintenance m = maintenanceRepo.findById(maintenanceId)
                .orElseThrow(() -> new IllegalStateException("Maintenance not found"));

        m.setStatus(MaintenanceStatus.DONE);
        m.setNotes((m.getNotes() == null ? "" : m.getNotes() + "\n") + (dto.getNotes() == null ? "" : dto.getNotes()));
        m.setClosedDate(LocalDate.now());
        maintenanceRepo.save(m);

        Asset asset = m.getAsset();

        if (dto.isRestoreToAssigned() && asset.getEmployee() != null) {
            asset.setStatus(AssetStatus.ASSIGNED_CONFIRMED);
        } else {
            asset.setStatus(AssetStatus.IN_STOCK);
            asset.setEmployee(null);
            asset.setConfirmed(false);
            asset.setAssignedDate(null);
        }
        assetRepo.save(asset);

        writeEvent(asset, EventType.MAINTENANCE_CLOSED, dto.getNotes());
        return assetMapper.toResponse(m);
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------

    private Asset getActiveByIdAndScope(Long id) {
        Asset asset = assetRepo.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalStateException("Asset not found or inactive"));
        companyService.assertInCurrentCompany(asset.getCompany().getId());
        return asset;
    }

    private void guardAssign(Asset asset) {
        if (asset.getStatus() != AssetStatus.IN_STOCK) {
            throw new IllegalStateException("Asset is not available for assignment");
        }
    }

    private void writeEvent(Asset asset, EventType type, String note) {
        AssetEvent ev = AssetEvent.builder()
                .asset(asset)
                .type(type)
                .actor(userService.getCurrentUserOrNull())
                .metadataJson(note) // keeping String for now; can switch to JSON later
                .build();
        eventRepo.save(ev);
    }
}
