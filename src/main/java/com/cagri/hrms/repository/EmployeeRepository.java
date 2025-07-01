package com.cagri.hrms.repository;

import com.cagri.hrms.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findAllByCompanyId(Long companyId);

    boolean existsByCompanyId(Long companyId);

    Optional<Employee> findByEmail(String email);
}
