package com.cagri.hrms.repository;

import com.cagri.hrms.entity.employee.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    // Finds all shifts by company id
    List<Shift> findAllByCompany_Id(Long companyId);
}
