package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.company.CompanyRequestDTO;
import com.cagri.hrms.dto.response.company.CompanyResponseDTO;
import com.cagri.hrms.dto.response.user.UserResponseDTO;

import java.util.List;

public interface CompanyService {

    CompanyResponseDTO createCompany(CompanyRequestDTO dto);

    List<CompanyResponseDTO> getAllCompanies();

    CompanyResponseDTO getCompanyById(Long id);

    CompanyResponseDTO updateCompany(Long id, CompanyRequestDTO dto);

    void deleteCompany(Long id);

    CompanyResponseDTO approvePendingCompany(Long userId);

    List<UserResponseDTO> getPendingCompanyManagers();

}
