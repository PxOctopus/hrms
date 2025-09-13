package com.cagri.hrms.repository;

import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.entity.expense.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Page<Project> findByCompanyAndActiveTrue(Company company, Pageable pageable);

    Optional<Project> findByCompanyAndIsGenericTrue(Company company);

    boolean existsByCompanyAndNameIgnoreCase(Company company, String name);
}
