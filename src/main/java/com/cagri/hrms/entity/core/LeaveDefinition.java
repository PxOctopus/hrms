package com.cagri.hrms.entity.core;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_leave_definitions")
public class LeaveDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;          // e.g. "Annual Leave", "Sick Leave"

    @Column(name = "max_days")    // NULL => unlimited
    private Integer maxDays;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "is_annual", nullable = false)
    @Builder.Default
    private boolean isAnnual = false; // true only for Annual
}
