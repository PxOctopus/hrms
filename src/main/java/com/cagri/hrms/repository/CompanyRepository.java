package com.cagri.hrms.repository;

import com.cagri.hrms.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);

    boolean existsByName(String name);
}
