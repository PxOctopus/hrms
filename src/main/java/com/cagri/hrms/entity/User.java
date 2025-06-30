package com.cagri.hrms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "tbl_users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // PERSONAL DETAILS
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    // AUTHENTICATION & STATUS
    @Column(nullable = false)
    @JsonIgnore // Hidden during JSON serialization for security reasons
    private String password;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    private Boolean isActive;

    // AUDIT
    @Column(name = "created_at")
    private LocalDate createdAt;

    // RELATIONS
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id") // Foreign key referencing the role table
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id") // Foreign key referencing the company table
    private Company company;
}

