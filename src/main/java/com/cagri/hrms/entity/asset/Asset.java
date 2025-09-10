package com.cagri.hrms.entity.asset;

import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.enums.AssetCondition;
import com.cagri.hrms.enums.AssetStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "assets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_company_serial", columnNames = {"company_id", "serial_number"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Human-readable name (e.g., "Dell Latitude 7420")
    @Column(nullable = false)
    private String assetName;

    // Manufacturer serial or internal tag; unique per company
    @Column(name = "serial_number", nullable = false)
    private String serialNumber;

    // (Laptop, Phone, Monitor, etc.)
    private String category;

    @Lob
    private String description;

    // Current lifecycle status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status; // IN_STOCK, ASSIGNED, ASSIGNED_CONFIRMED, MAINTENANCE, RETURN_REQUESTED, LOST, RETIRED

    // Physical/functional condition label
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetCondition condition; // NEW, GOOD, FAIR, POOR, DAMAGED

    // Who currently holds the asset (nullable if IN_STOCK/RETIRED/etc.)
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    // Manager who last assigned/updated this asset
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    // Owning company
    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id")
    private Company company;

    // Assignment metadata
    private LocalDate assignedDate;

    // Employee confirmed receiving the asset
    private boolean confirmed;

    // Soft-delete flag
    @Column(nullable = false)
    private boolean active = true;

    // Optional location info (Office, Warehouse shelf, etc.)
    private String location;

    // Audit
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        // default state for new inventory items is IN_STOCK unless explicitly assigned
        if (this.status == null) this.status = AssetStatus.IN_STOCK;
        if (this.condition == null) this.condition = AssetCondition.NEW;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
