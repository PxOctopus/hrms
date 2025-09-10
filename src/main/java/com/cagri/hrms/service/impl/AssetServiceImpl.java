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

    // Mappers
    private final AssetMapper assetMapper;                     // FULL
    private final EmployeeAssetMapper employeeAssetMapper;     // SLIM

    // Context services
    private final UserService userService;
    private final EmployeeService employeeService;
    private final CompanyService companyService;

    // ---------------- Manager flows (return FULL DTO) ----------------

    @Override
    public AssetResponseDTO create(AssetCreateRequestDTO dto) {
        // Resolve company from auth context
        Company company = companyService.getCurrentCompanyOrThrow();

        Asset entity = assetMapper.toEntity(dto);
        entity.setCompany(company);
        entity.setStatus(AssetStatus.IN_STOCK);

        // NEW: ensure defaults so it appears in manager list and starts unconfirmed
        entity.setActive(true);              // <-- list uses findByCompanyIdAndActiveTrue
        entity.setConfirmed(false);          // <-- brand-new, not yet confirmed by an employee

        if (entity.getCondition() == null) {
            entity.setCondition(AssetCondition.NEW);
        }

        entity = assetRepo.save(entity);
        writeEvent(entity, EventType.NOTE_ADDED, "Created asset");
        return assetMapper.toResponse(entity); // FULL
    }

    @Override
    public AssetResponseDTO update(Long id, AssetUpdateRequestDTO dto) {
        Asset asset = getActiveByIdAndScope(id);
        assetMapper.updateEntity(asset, dto);
        Asset saved = assetRepo.save(asset);
        writeEvent(saved, EventType.NOTE_ADDED, "Updated asset fields");
        return assetMapper.toResponse(saved); // FULL
    }

    @Override
    public void softDelete(Long id) {
        Asset asset = getActiveByIdAndScope(id);
        // If you use "deleted" flag instead of "active", adjust here accordingly
        asset.setActive(false);
        assetRepo.save(asset);
        writeEvent(asset, EventType.NOTE_ADDED, "Soft-deleted");
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetResponseDTO> listByCompany(Long companyId, AssetStatus status) {
        // Note: list is company-scoped + active=true
        List<Asset> list = (status == null)
                ? assetRepo.findByCompanyIdAndActiveTrue(companyId)
                : assetRepo.findByCompanyIdAndStatusAndActiveTrue(companyId, status);

        return list.stream().map(assetMapper::toResponse).toList(); // FULL
    }

    @Override
    public AssetResponseDTO assign(Long assetId, AssetAssignRequestDTO dto) {
        Asset asset = getActiveByIdAndScope(assetId);
        guardAssign(asset);

        Employee emp = employeeService.getByIdScoped(dto.getEmployeeId());
        User manager = userService.getCurrentUserOrThrow();

        // NEW: record who assigned it (manager), and set assignment fields
        asset.setEmployee(emp);
        asset.setManager(manager);                   // <-- manager_id will be persisted
        asset.setStatus(AssetStatus.ASSIGNED);
        asset.setAssignedDate(LocalDate.now());
        asset.setConfirmed(false);                   // waiting for employee confirmation

        Asset saved = assetRepo.save(asset);
        writeEvent(saved, EventType.ASSIGNED, dto.getNote());
        return assetMapper.toResponse(saved); // FULL
    }

    @Override
    public AssetResponseDTO changeStatus(Long assetId, AssetChangeStatusRequestDTO dto) {
        Asset asset = getActiveByIdAndScope(assetId);

        AssetStatus oldStatus = asset.getStatus();
        AssetStatus newStatus = dto.getStatus();

        // GUARD: RETIRED is terminal; cannot transition away from it.
        if (oldStatus == AssetStatus.RETIRED && newStatus != AssetStatus.RETIRED) {
            throw new IllegalStateException("Retired assets cannot change state.");
        }

        // SPECIAL: Manager "confirm" for issue states:
        // If same status is sent while in MAINTENANCE/LOST/RETIRED, do NOT change lifecycle state;
        // just write an ISSUE_CONFIRMED event for audit purposes.
        if (newStatus == oldStatus &&
                (oldStatus == AssetStatus.MAINTENANCE || oldStatus == AssetStatus.LOST || oldStatus == AssetStatus.RETIRED)) {
            // NEW: explicit confirmation event (you added ISSUE_CONFIRMED)
            writeEvent(asset, EventType.ISSUE_CONFIRMED, dto.getNote() != null ? dto.getNote() : "Issue confirmed");
            Asset persisted = assetRepo.save(asset); // persist event relation
            return assetMapper.toResponse(persisted);
        }

        // MARK IN STOCK: only allowed from MAINTENANCE / LOST / RETURN_REQUESTED
        if (newStatus == AssetStatus.IN_STOCK) {
            if (!(oldStatus == AssetStatus.MAINTENANCE
                    || oldStatus == AssetStatus.LOST
                    || oldStatus == AssetStatus.RETURN_REQUESTED)) {
                throw new IllegalStateException("Only MAINTENANCE, LOST or RETURN_REQUESTED can be moved to IN_STOCK.");
            }

            // NEW: When going back to stock, clear assignment fields.
            asset.setEmployee(null);
            asset.setConfirmed(false);
            asset.setAssignedDate(null);

            asset.setStatus(AssetStatus.IN_STOCK);
            Asset savedStock = assetRepo.save(asset);
            writeEvent(savedStock, EventType.STATUS_CHANGED, dto.getNote() != null ? dto.getNote() : "Marked IN_STOCK");
            return assetMapper.toResponse(savedStock);
        }

        // DEFAULT: other transitions (manager manual status change)
        asset.setStatus(newStatus);
        Asset saved = assetRepo.save(asset);
        writeEvent(saved, EventType.STATUS_CHANGED, dto.getNote());
        return assetMapper.toResponse(saved);
    }

    // ---------------- Employee self-service ----------------

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeAssetResponseDTO> listMyAssets(Long employeeId) {
        return assetRepo.findByEmployeeIdAndActiveTrue(employeeId)
                .stream()
                .map(employeeAssetMapper::toEmployeeDto) // SLIM
                .toList();
    }

    @Override
    public AssetResponseDTO confirm(Long assetId, AssetConfirmRequestDTO dto, Long employeeId) {
        Asset asset = getActiveByIdAndScope(assetId);

        // Only assigned employee can confirm
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
        return assetMapper.toResponse(saved); // FULL (FE needs status/confirmed etc.)
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
        return assetMapper.toResponse(saved); // FULL
    }

    @Override
    public AssetResponseDTO reportIssue(Long assetId, AssetIssueReportRequestDTO dto, Long employeeId) {
        Asset asset = getActiveByIdAndScope(assetId);

        // GUARD: Only the assigned employee can report an issue.
        if (asset.getEmployee() == null || !asset.getEmployee().getId().equals(employeeId)) {
            throw new IllegalStateException("Not authorized to report issue");
        }

        // GUARD: Retired is terminal; no further modifications.
        if (asset.getStatus() == AssetStatus.RETIRED) {
            throw new IllegalStateException("Retired assets cannot be modified.");
        }

        // GUARD: Only these "issue" transitions from UI are allowed here.
        AssetStatus target = dto.getIssueType();
        if (target != AssetStatus.MAINTENANCE &&
                target != AssetStatus.LOST &&
                target != AssetStatus.RETIRED) {
            throw new IllegalArgumentException("Only MAINTENANCE, LOST or RETIRED are allowed for issue report.");
        }

        // CHANGE: Set new status directly based on employee's report.
        AssetStatus old = asset.getStatus();
        asset.setStatus(target);

        // WRITE EVENT: Use specific event types for better auditability.
        if (target == AssetStatus.MAINTENANCE) {
            writeEvent(asset, EventType.MAINTENANCE_OPENED, "Reported by employee");
        } else if (target == AssetStatus.LOST) {
            writeEvent(asset, EventType.LOST_REPORTED, "Reported by employee");
        } else { // RETIRED
            // NOTE: RETIRED is terminal; manager cannot "mark in stock" afterwards.
            writeEvent(asset, EventType.STATUS_CHANGED, "Retired by employee");
        }

        Asset saved = assetRepo.save(asset);
        return assetMapper.toResponse(saved); // FULL
    }
    // ---------------- Events & Maintenance ----------------

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

        // Restore status depending on configuration
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

    // ---------------- Helpers ----------------

    private Asset getActiveByIdAndScope(Long id) {
        Asset asset = assetRepo.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalStateException("Asset not found or inactive"));
        // Company scoping check
        companyService.assertInCurrentCompany(asset.getCompany().getId());
        return asset;
    }

    private void guardAssign(Asset asset) {
        // Basic guard: only assign if in inventory-like states
        if (asset.getStatus() != AssetStatus.IN_STOCK) {
            throw new IllegalStateException("Asset is not available for assignment");
        }
    }

    private void writeEvent(Asset asset, EventType type, String note) {
        AssetEvent ev = AssetEvent.builder()
                .asset(asset)
                .type(type)
                .actor(userService.getCurrentUserOrNull())
                .metadataJson(note)
                .build();
        eventRepo.save(ev);
    }
}
