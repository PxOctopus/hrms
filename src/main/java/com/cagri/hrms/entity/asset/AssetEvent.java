package com.cagri.hrms.entity.asset;

import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.enums.EventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "asset_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which asset this event belongs to
    @ManyToOne(optional = false)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    // Event type: ASSIGNED, CONFIRMED, RETURN_REQUESTED, RETURN_COMPLETED, STATUS_CHANGED, CONDITION_CHANGED, MAINTENANCE_OPENED, MAINTENANCE_CLOSED, ISSUE_REPORTED, LOST_REPORTED, NOTE_ADDED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    // Optional actor
    @ManyToOne
    @JoinColumn(name = "actor_user_id")
    private User actor;

    // Free-form details (who/why/how)
    @Lob
    private String metadataJson;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { this.createdAt = LocalDateTime.now(); }

}

