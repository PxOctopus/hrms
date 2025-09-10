package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.company.CompanyRequestDTO;
import com.cagri.hrms.dto.response.company.CompanyResponseDTO;
import com.cagri.hrms.dto.response.user.UserResponseDTO;
import com.cagri.hrms.entity.core.Company;

import java.util.List;

public interface CompanyService {

    CompanyResponseDTO createCompany(CompanyRequestDTO dto);

    List<CompanyResponseDTO> getAllCompanies();

    CompanyResponseDTO getCompanyById(Long id);

    CompanyResponseDTO updateCompany(Long id, CompanyRequestDTO dto);

    void deleteCompany(Long id);

    CompanyResponseDTO approvePendingCompany(Long userId);

    List<UserResponseDTO> getPendingCompanyManagers();

// ---------------- ADDED: multi-tenant scope helpers ----------------
    /** Returns current company entity inferred from the authenticated principal. */
    // ADDED:
    Company getCurrentCompanyOrThrow();

    /** Convenience: current company id or throws. */
    // ADDED:
    Long getCurrentCompanyIdOrThrow();

    /** Guards cross-company access (throws 403/404 depending on your policy). */
    // ADDED:
    void assertInCurrentCompany(Long companyId);

    /** Convenience passthrough to user id (some controllers call this). */
    // ADDED:
    Long getCurrentUserId();
}
