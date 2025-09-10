package com.cagri.hrms.repository;

import com.cagri.hrms.entity.asset.AssetMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetMaintenanceRepository extends JpaRepository<AssetMaintenance, Long> {
    List<AssetMaintenance> findByAssetId(Long assetId);
}
