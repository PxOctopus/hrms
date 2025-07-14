package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.employee.AssetRequestDTO;
import com.cagri.hrms.dto.response.employee.AssetResponseDTO;
import com.cagri.hrms.entity.core.Asset;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.mapper.AssetMapper;
import com.cagri.hrms.repository.AssetRepository;
import com.cagri.hrms.repository.EmployeeRepository;
import com.cagri.hrms.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final EmployeeRepository employeeRepository;
    private final AssetMapper assetMapper;

    @Override
    public void addAsset(AssetRequestDTO dto) {
        Asset asset = assetMapper.toEntity(dto);
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        asset.setEmployee(employee);
        assetRepository.save(asset);
    }

    @Override
    public List<AssetResponseDTO> getAllAssets() {
        return assetRepository.findAll()
                .stream()
                .map(assetMapper::toDto)
                .toList();
    }
    @Override
    public List<AssetResponseDTO> getAssetsOfActiveEmployees() {
        return assetRepository.findAllByEmployee_IsActiveTrue()
                .stream()
                .map(assetMapper::toDto)
                .toList();
    }

    @Override
    public List<AssetResponseDTO> getAssetsByEmployeeId(Long employeeId) {
        return assetRepository.findAllByEmployee_IdAndEmployee_IsActiveTrue(employeeId)
                .stream()
                .map(assetMapper::toDto)
                .toList();
    }

    @Override
    public List<AssetResponseDTO> getAssetsOfLoggedInEmployee() {
        // Get the currently authenticated user from the security context
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Find the corresponding Employee entity for the logged-in user
        Employee employee = employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Retrieve all assets assigned to this employee
        List<Asset> assets = assetRepository.findByEmployee(employee);

        // Map the Asset entities to DTOs and return as a list
        return assets.stream()
                .map(assetMapper::toDto)
                .toList();
    }
}


