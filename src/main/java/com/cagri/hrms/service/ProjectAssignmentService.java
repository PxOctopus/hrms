package com.cagri.hrms.service;

import com.cagri.hrms.dto.response.expense.ProjectAssignmentResponseDTO;
import com.cagri.hrms.entity.employee.Employee;

import java.util.List;

public interface ProjectAssignmentService {

    ProjectAssignmentResponseDTO assignEmployee(Long projectId, Long employeeId, Long managerUserId);

    ProjectAssignmentResponseDTO respondAssignment(Long assignmentId, boolean accepted, Long employeeUserId);

    List<ProjectAssignmentResponseDTO> listForEmployee(Employee employee);
}
