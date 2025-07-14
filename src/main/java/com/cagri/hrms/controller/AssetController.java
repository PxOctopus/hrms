package com.cagri.hrms.controller;

import com.cagri.hrms.dto.request.employee.AssetRequestDTO;
import com.cagri.hrms.dto.response.employee.AssetResponseDTO;
import com.cagri.hrms.service.AssetService;
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

    // Only users with the MANAGER role can add a new asset
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> addAsset(@RequestBody AssetRequestDTO dto) {
        assetService.addAsset(dto);
        return ResponseEntity.ok().build();
    }

    // Retrieve all assets, accessible only by the MANAGER role
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<AssetResponseDTO>> getAllAssets() {
        return ResponseEntity.ok(assetService.getAllAssets());
    }

    // Retrieve all assets assigned to active employees, only accessible by MANAGER
    @GetMapping("/active-employees")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<AssetResponseDTO>> getAssetsOfActiveEmployees() {
        return ResponseEntity.ok(assetService.getAssetsOfActiveEmployees());
    }

    // Retrieve assets assigned to a specific employee, accessible only by MANAGER
    @GetMapping("/by-employee/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<AssetResponseDTO>> getAssetsByEmployeeId(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.getAssetsByEmployeeId(id));
    }

    // Employees can view their own assigned assets
    @GetMapping("/my-assets")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<AssetResponseDTO>> getMyAssets() {
        return ResponseEntity.ok(assetService.getAssetsOfLoggedInEmployee());
    }
}
