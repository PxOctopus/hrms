package com.cagri.hrms.repository;

import com.cagri.hrms.dto.request.company.CompanyRequestDTO;
import com.cagri.hrms.dto.response.company.CompanyResponseDTO;
import com.cagri.hrms.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByCompanyNameIgnoreCase(String name);

    int countByIsActiveTrue();

    boolean existsByIdAndIsActiveTrue(Long companyId);

    Optional<Company> findByCompanyNameAndCompanyEmail(String companyName, String companyEmail);
}
