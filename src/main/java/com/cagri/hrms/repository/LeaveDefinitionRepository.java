package com.cagri.hrms.repository;

import com.cagri.hrms.entity.core.LeaveDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeaveDefinitionRepository extends JpaRepository<LeaveDefinition, Long> {
    Optional<LeaveDefinition> findByNameIgnoreCase(String name);

    Optional<LeaveDefinition> findFirstByIsAnnualTrue();
}
