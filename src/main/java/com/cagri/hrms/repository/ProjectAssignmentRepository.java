//package com.cagri.hrms.repository;
//
//import com.cagri.hrms.entity.employee.Employee;
//import com.cagri.hrms.entity.expense.Project;
//import com.cagri.hrms.entity.expense.ProjectAssignment;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface ProjectAssignmentRepository extends JpaRepository<ProjectAssignment, Long> {
//
//    // Returns the unique assignment of an employee to a specific project (enforced by unique constraint)
//    Optional<ProjectAssignment> findByProjectAndEmployee(Project project, Employee employee);
//
//    // Lists all assignments for a given employee
//    List<ProjectAssignment> findByEmployee(Employee employee);
//
//    // Lists all assignments for a given project
//    List<ProjectAssignment> findByProject(Project project);
//}
