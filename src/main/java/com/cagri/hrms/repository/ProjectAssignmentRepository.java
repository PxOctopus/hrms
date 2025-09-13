package com.cagri.hrms.repository;

import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.expense.Project;
import com.cagri.hrms.entity.expense.ProjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, Long> {

    Optional<ProjectAssignment> findByProjectAndEmployee(Project project, Employee employee);

    List<ProjectAssignment> findByEmployee(Employee employee);

    List<ProjectAssignment> findByProject(Project project);
}
