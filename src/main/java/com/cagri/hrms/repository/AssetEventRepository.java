package com.cagri.hrms.repository;

import com.cagri.hrms.entity.asset.AssetEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetEventRepository extends JpaRepository<AssetEvent, Long> {
    List<AssetEvent> findByAssetIdOrderByCreatedAtAsc(Long assetId);
}
