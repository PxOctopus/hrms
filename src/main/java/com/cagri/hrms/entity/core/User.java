package com.cagri.hrms.entity.core;

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
    @JsonIgnore // Password should not be returned in API responses
    private String password;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "pending_company_name")
    private String pendingCompanyName;

    @Column(name = "enabled")
    private boolean enabled; // Used by Spring Security for account status (true = enabled)

    private Boolean isActive; // Custom flag to indicate logical activation (for HR usage)

    @Column(name = "verification_token")
    private String verificationToken; // Token sent during registration for email confirmation

    // PASSWORD RESET SUPPORT
    @Column(name = "reset_token")
    private String resetToken; // Token for password reset email

    @Column(name = "reset_token_expiry")
    private LocalDate resetTokenExpiry; // Expiry date for password reset token

    // AUDIT
    @Column(name = "created_at")
    private LocalDate createdAt;

    // RELATIONS
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;


}
