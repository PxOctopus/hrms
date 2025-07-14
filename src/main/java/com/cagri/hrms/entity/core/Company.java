package com.cagri.hrms.entity.core;

import com.cagri.hrms.enums.EmployeeLimitLevel;
import com.cagri.hrms.enums.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "tbl_companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // COMPANY DETAILS
    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_email", nullable = false, unique = true)
    private String companyEmail;

    @Builder.Default
    @Column(name = "number_of_employees")
    private Integer numberOfEmployees = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_limit_level")
    private EmployeeLimitLevel employeeLimitLevel;

    @Column(name = "employee_number_limit")
    private Integer employeeNumberLimit; // Max number of employees allowed

    @Column(name = "is_active")
    private boolean isActive;

    // RELATIONS
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_user_id") // foreign key to tbl_user
    private User companyManager;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan")
    private SubscriptionPlan subscriptionPlan;

    // AUDIT
    @Builder.Default
    @Column(name = "created_at")
    private Long createAt = System.currentTimeMillis();

    @Column(name = "updated_at")
    private Long updateAt;

}
