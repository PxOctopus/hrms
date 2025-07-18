package com.cagri.hrms.repository;

import com.cagri.hrms.entity.core.LeaveDefinition;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.employee.Leave;
import com.cagri.hrms.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRepository extends JpaRepository<Leave, Long> {

    // Find all leaves of a given employee
    List<Leave> findByEmployee(Employee employee);

    // Find all leaves of an employee with specific status
    List<Leave> findByEmployeeAndStatus(Employee employee, LeaveStatus status);

    // Find all leaves with a specific status (e.g., APPROVED, PENDING)
    List<Leave> findAllByStatus(LeaveStatus status);

    // Find leaves by type (if needed for reports, limits, etc.)
    List<Leave> findByLeaveDefinition(LeaveDefinition definition);
}
