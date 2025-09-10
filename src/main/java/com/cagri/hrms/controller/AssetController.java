package com.cagri.hrms.controller;

import com.cagri.hrms.dto.request.asset.*;
import com.cagri.hrms.dto.request.employee.AssetRequestDTO;
import com.cagri.hrms.dto.response.asset.AssetEventResponseDTO;
import com.cagri.hrms.dto.response.asset.AssetMaintenanceResponseDTO;
import com.cagri.hrms.dto.response.employee.AssetResponseDTO;
import com.cagri.hrms.enums.AssetStatus;
import com.cagri.hrms.service.AssetService;
import com.cagri.hrms.service.CompanyService;
import com.cagri.hrms.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final CompanyService companyService;
    private final EmployeeService employeeService;

    // -------- Manager endpoints --------

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AssetResponseDTO> create(@RequestBody AssetCreateRequestDTO dto) {
        return ResponseEntity.ok(assetService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AssetResponseDTO> update(@PathVariable Long id, @RequestBody AssetUpdateRequestDTO dto) {
        return ResponseEntity.ok(assetService.update(id, dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<AssetResponseDTO>> list(
            @RequestParam(required = false) AssetStatus status
    ) {
        Long companyId = companyService.getCurrentCompanyOrThrow().getId();
        return ResponseEntity.ok(assetService.listByCompany(companyId, status));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AssetResponseDTO> assign(@PathVariable Long id, @RequestBody AssetAssignRequestDTO dto) {
        return ResponseEntity.ok(assetService.assign(id, dto));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AssetResponseDTO> changeStatus(@PathVariable Long id, @RequestBody AssetChangeStatusRequestDTO dto) {
        return ResponseEntity.ok(assetService.changeStatus(id, dto));
    }

    @PostMapping("/{id}/approve-return")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AssetResponseDTO> approveReturn(@PathVariable Long id) {
        // Convenience: equivalent to changeStatus(IN_STOCK) + clear employee
        AssetChangeStatusRequestDTO dto = AssetChangeStatusRequestDTO.builder()
                .status(AssetStatus.IN_STOCK)
                .note("Return approved")
                .build();
        return ResponseEntity.ok(assetService.changeStatus(id, dto));
    }

    @PostMapping("/{id}/maintenance")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AssetMaintenanceResponseDTO> openMaintenance(@PathVariable Long id,
                                                                       @RequestBody MaintenanceOpenRequestDTO dto) {
        Long actorUserId = companyService.getCurrentUserId();
        return ResponseEntity.ok(assetService.openMaintenance(id, dto, actorUserId));
    }

    // -------- Employee endpoints --------

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<AssetResponseDTO>> myAssets() {
        Long employeeId = employeeService.getCurrentEmployeeIdOrThrow();
        return ResponseEntity.ok(assetService.listMyAssets(employeeId));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AssetResponseDTO> confirm(@PathVariable Long id, @RequestBody AssetConfirmRequestDTO dto) {
        Long employeeId = employeeService.getCurrentEmployeeIdOrThrow();
        return ResponseEntity.ok(assetService.confirm(id, dto, employeeId));
    }

    @PostMapping("/{id}/request-return")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AssetResponseDTO> requestReturn(@PathVariable Long id, @RequestBody AssetReturnRequestDTO dto) {
        Long employeeId = employeeService.getCurrentEmployeeIdOrThrow();
        return ResponseEntity.ok(assetService.requestReturn(id, dto, employeeId));
    }

    @PostMapping("/{id}/report-issue")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AssetResponseDTO> reportIssue(@PathVariable Long id, @RequestBody AssetIssueReportRequestDTO dto) {
        Long employeeId = employeeService.getCurrentEmployeeIdOrThrow();
        return ResponseEntity.ok(assetService.reportIssue(id, dto, employeeId));
    }

    // -------- Common endpoints --------

    @GetMapping("/{id}/events")
    @PreAuthorize("hasAnyRole('MANAGER','EMPLOYEE')")
    public ResponseEntity<List<AssetEventResponseDTO>> events(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.events(id));
    }
}
