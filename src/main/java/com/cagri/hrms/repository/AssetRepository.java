package com.cagri.hrms.repository;

import com.cagri.hrms.entity.core.Asset;
import com.cagri.hrms.entity.employee.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {


    List<Asset> findByEmployee(Employee employee);

    List<Asset> findByEmployeeAndIsActiveTrue(Employee employee);

    // Finds all assets assigned to active employees
    List<Asset> findAllByEmployee_IsActiveTrue();

    // Finds all assets of a specific employee if the employee is active
    List<Asset> findAllByEmployee_IdAndEmployee_IsActiveTrue(Long employeeId);


}
