package com.cagri.hrms.entity.asset;

import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.enums.MaintenanceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "asset_maintenances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetMaintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    // Requested by (employee) and handled by (manager/vendor)
    @ManyToOne @JoinColumn(name = "requested_by_id")
    private User requestedBy;

    private String vendorName;
    private String ticketNumber;

    // OPEN, IN_PROGRESS, DONE, CANCELLED
    @Enumerated(EnumType.STRING)
    private MaintenanceStatus status;

    @Lob
    private String notes;

    private LocalDate openedDate;
    private LocalDate closedDate;


}
