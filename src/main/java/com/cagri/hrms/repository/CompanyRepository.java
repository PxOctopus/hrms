package com.cagri.hrms.repository;

import com.cagri.hrms.entity.core.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    // Case-insensitive uniqueness check for company name
    boolean existsByCompanyNameIgnoreCase(String name);

    // Count active companies (if you track global activation)
    int countByIsActiveTrue();

    // Guard: quick check if a company is active
    boolean existsByIdAndIsActiveTrue(Long companyId);

    // Resolve company by name and matching email domain
    Optional<Company> findByCompanyNameIgnoreCaseAndCompanyEmailEndingWithIgnoreCase(String companyName, String domain);

    // DEPRECATED: Prefer the nested property variant below.
    // Kept only for backward compatibility. Remove if unused in codebase.
//    Optional<Company> findByCompanyManagerId(Long managerUserId);

    /** Preferred: nested property path for manager.user.id */
    Optional<Company> findByCompanyManager_Id(Long userId);
}
