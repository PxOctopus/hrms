package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.employee.AssetRequestDTO;
import com.cagri.hrms.dto.response.employee.AssetResponseDTO;

import java.util.List;

public interface AssetService {
    void addAsset(AssetRequestDTO dto);

    List<AssetResponseDTO> getAllAssets();

    List<AssetResponseDTO> getAssetsOfActiveEmployees();

    List<AssetResponseDTO> getAssetsByEmployeeId(Long employeeId);

    List<AssetResponseDTO> getAssetsOfLoggedInEmployee();
}
