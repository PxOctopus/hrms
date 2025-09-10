package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.asset.*;
import com.cagri.hrms.dto.response.asset.AssetEventResponseDTO;
import com.cagri.hrms.dto.response.asset.AssetMaintenanceResponseDTO;
import com.cagri.hrms.dto.response.employee.AssetResponseDTO;
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
import com.cagri.hrms.mapper.AssetMapper;
import com.cagri.hrms.repository.AssetEventRepository;
import com.cagri.hrms.repository.AssetMaintenanceRepository;
import com.cagri.hrms.repository.AssetRepository;
import com.cagri.hrms.service.AssetService;
import com.cagri.hrms.service.CompanyService;
import com.cagri.hrms.service.EmployeeService;
import com.cagri.hrms.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepo;
    private final AssetEventRepository eventRepo;
    private final AssetMaintenanceRepository maintenanceRepo;
    private final AssetMapper assetMapper;

    // Inject user/company/employee services to resolve current context
    private final UserService userService;
    private final EmployeeService employeeService;
    private final CompanyService companyService;

    @Override
    public AssetResponseDTO create(AssetCreateRequestDTO dto) {
        // Resolve company from auth context
        Company company = companyService.getCurrentCompanyOrThrow();
        Asset entity = assetMapper.toEntity(dto);
        entity.setCompany(company);
        entity.setStatus(AssetStatus.IN_STOCK);
        if (entity.getCondition() == null) entity.setCondition(AssetCondition.NEW);
        entity = assetRepo.save(entity);
        writeEvent(entity, EventType.NOTE_ADDED, "Created asset");
        return assetMapper.toResponse(entity);
    }

    @Override
    public AssetResponseDTO update(Long id, AssetUpdateRequestDTO dto) {
        Asset asset = getActiveByIdAndScope(id);
        assetMapper.updateEntity(asset, dto);
        Asset saved = assetRepo.save(asset);
        writeEvent(saved, EventType.NOTE_ADDED, "Updated asset fields");
        return assetMapper.toResponse(saved);
    }

    @Override
    public void softDelete(Long id) {
        Asset asset = getActiveByIdAndScope(id);
        asset.setActive(false);
        assetRepo.save(asset);
        writeEvent(asset, EventType.NOTE_ADDED, "Soft-deleted");
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetResponseDTO> listByCompany(Long companyId, AssetStatus status) {
        List<Asset> list = (status == null)
                ? assetRepo.findByCompanyIdAndActiveTrue(companyId)
                : assetRepo.findByCompanyIdAndStatusAndActiveTrue(companyId, status);
        return list.stream().map(assetMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetResponseDTO> listMyAssets(Long employeeId) {
        return assetRepo.findByEmployeeIdAndActiveTrue(employeeId)
                .stream().map(assetMapper::toResponse).toList();
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
        return assetMapper.toResponse(saved);
    }

    @Override
    public AssetResponseDTO changeStatus(Long assetId, AssetChangeStatusRequestDTO dto) {
        Asset asset = getActiveByIdAndScope(assetId);
        // Guard transitions (simple MVP rules)
        switch (dto.getStatus()) {
            case IN_STOCK -> {
                asset.setEmployee(null);
                asset.setConfirmed(false);
            }
            case RETIRED, LOST -> {
                // Terminal-like transitions (no auto re-open)
            }
            case MAINTENANCE -> {
                // Keep employee as-is or null; depends on your policy
            }
            default -> {}
        }
        asset.setStatus(dto.getStatus());
        Asset saved = assetRepo.save(asset);
        writeEvent(saved, EventType.STATUS_CHANGED, dto.getNote());
        return assetMapper.toResponse(saved);
    }

    @Override
    public AssetResponseDTO confirm(Long assetId, AssetConfirmRequestDTO dto, Long employeeId) {
        Asset asset = getActiveByIdAndScope(assetId);
        // Only the assigned employee can confirm
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
        return assetMapper.toResponse(saved);
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
        return assetMapper.toResponse(saved);
    }

    @Override
    public AssetResponseDTO reportIssue(Long assetId, AssetIssueReportRequestDTO dto, Long employeeId) {
        Asset asset = getActiveByIdAndScope(assetId);
        if (asset.getEmployee() == null || !asset.getEmployee().getId().equals(employeeId)) {
            throw new IllegalStateException("Not authorized to report issue");
        }
        writeEvent(asset, EventType.ISSUE_REPORTED, dto.getIssueType() + " - " + dto.getDescription());
        return assetMapper.toResponse(asset);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetEventResponseDTO> events(Long assetId) {
        return eventRepo.findByAssetIdOrderByCreatedAtAsc(assetId)
                .stream().map(assetMapper::toResponse).toList();
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
        }
        assetRepo.save(asset);

        writeEvent(asset, EventType.MAINTENANCE_CLOSED, dto.getNotes());
        return assetMapper.toResponse(m);
    }

    // --------- Helpers ---------

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


