package com.cagri.hrms.repository;

import com.cagri.hrms.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findAllByCompanyId(Long companyId);

    boolean existsByCompanyId(Long companyId);

    Optional<Employee> findByEmail(String email);

    int countByCompanyId(Long companyId);
}
