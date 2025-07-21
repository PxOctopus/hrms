package com.cagri.hrms.repository;

import com.cagri.hrms.entity.employee.EmployeeShift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, Long> {
    // Finds all shifts assigned to a specific employee
    List<EmployeeShift> findAllByEmployee_Id(Long employeeId);

    // Finds all employee shifts for a specific shift (definition)
    List<EmployeeShift> findAllByShift_Id(Long shiftId);
}
