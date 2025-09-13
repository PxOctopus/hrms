package com.cagri.hrms.repository;

import com.cagri.hrms.entity.asset.AssetEvent;
import com.cagri.hrms.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AssetEventRepository extends JpaRepository<AssetEvent, Long> {

    List<AssetEvent> findByAssetIdOrderByCreatedAtAsc(Long assetId);

    // legacy check (any time in history)
    boolean existsByAssetIdAndType(Long assetId, EventType type);

    // NEW: fetch the latest "issue opened" style event (e.g., MAINTENANCE_OPENED, LOST_REPORTED, RETIREMENT_REQUESTED)
    Optional<AssetEvent> findTopByAssetIdAndTypeInOrderByCreatedAtDesc(
            Long assetId, Collection<EventType> types
    );

    // NEW: check if there's an ISSUE_CONFIRMED after a given timestamp
    boolean existsByAssetIdAndTypeAndCreatedAtAfter(
            Long assetId, EventType type, LocalDateTime createdAt
    );

}
