package com.cagri.hrms.repository;

import com.cagri.hrms.entity.employee.Break;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BreakRepository extends JpaRepository<Break, Long> {
    List<Break> findAllByEmployeeId(Long employeeId);
}
