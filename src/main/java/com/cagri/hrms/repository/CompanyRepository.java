package com.cagri.hrms.repository;

import com.cagri.hrms.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByCompanyName(String name);

    boolean existsByCompanyName(String name);

    int countByIsActiveTrue();

    boolean existsByIdAndIsActiveTrue(Long companyId);

}
