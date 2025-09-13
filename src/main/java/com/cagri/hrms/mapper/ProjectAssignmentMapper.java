package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.response.expense.ProjectAssignmentResponseDTO;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.expense.Project;
import com.cagri.hrms.entity.expense.ProjectAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectAssignmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", source = "project")
    @Mapping(target = "employee", source = "employee")
    @Mapping(target = "accepted", constant = "false")
    @Mapping(target = "assignedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "respondedAt", ignore = true)
    ProjectAssignment create(Project project, Employee employee);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "projectCode", source = "project.code")
    @Mapping(target = "projectGeneric", source = "project.generic")
    @Mapping(target = "employeeId", source = "employee.id")
    ProjectAssignmentResponseDTO toDTO(ProjectAssignment entity);
}
