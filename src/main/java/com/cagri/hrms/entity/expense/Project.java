package com.cagri.hrms.entity.expense;

import com.cagri.hrms.entity.core.Company;
import jakarta.persistence.*;
//import lombok.*;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "projects",
//        indexes = {@Index(name="ix_project_company_active", columnList = "company_id,active")})
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Project {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    // e.g., "PPL-2025-012"
//    @Column(length = 64)
//    private String code;
//
//    @Column(nullable = false, length = 200)
//    private String name;
//
//    @Column(length = 2000)
//    private String description;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    private Company company;
//
//    private boolean active = true;
//
//    // Generic = company-wide bucket (no assignment required)
//    private boolean isGeneric = false;
//
//    // If true, employees must accept assignment before using this project
//    private boolean requiresAssignment = false;
//
//    // Optional caps (nullable)
//    private BigDecimal budgetLimit;
//    private BigDecimal monthlyCap;
//
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//
//    @PrePersist void prePersist() {
//        this.createdAt = LocalDateTime.now();
//        this.updatedAt = this.createdAt;
//    }
//    @PreUpdate void preUpdate() {
//        this.updatedAt = LocalDateTime.now();
//    }
//}
