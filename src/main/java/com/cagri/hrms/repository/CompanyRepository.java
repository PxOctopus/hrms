package com.cagri.hrms.repository;

import com.cagri.hrms.entity.core.Company;
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
