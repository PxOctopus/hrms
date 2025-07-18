package com.cagri.hrms.repository;

import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.employee.Employee;
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

    Optional<Employee> findByUserId(Long userId);

    Optional<Employee> findByUser(User user);
}
