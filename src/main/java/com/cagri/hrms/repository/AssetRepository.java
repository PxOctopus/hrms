package com.cagri.hrms.repository;

import com.cagri.hrms.entity.asset.Asset;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.enums.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByIdAndActiveTrue(Long id);
    List<Asset> findByCompanyIdAndActiveTrue(Long companyId);
    List<Asset> findByCompanyIdAndStatusAndActiveTrue(Long companyId, AssetStatus status);
    List<Asset> findByEmployeeIdAndActiveTrue(Long employeeId);
    boolean existsByCompanyIdAndSerialNumber(Long companyId, String serialNumber);
}
