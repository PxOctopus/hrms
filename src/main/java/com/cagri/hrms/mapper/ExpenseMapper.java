package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.request.employee.ExpenseRequestDTO;
import com.cagri.hrms.dto.request.expense.ExpenseCreateDTO;
import com.cagri.hrms.dto.request.expense.ExpenseUpdateDTO;
import com.cagri.hrms.dto.response.expense.ExpenseResponseDTO;
import com.cagri.hrms.entity.expense.Expense;
import com.cagri.hrms.entity.expense.Project;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    // CreateDTO -> Entity (only safe fields; employee set in service)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true) // set from auth context
    @Mapping(target = "project", source = "projectId", qualifiedByName = "toProjectRef")
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "managerReviewedAt", ignore = true)
    @Mapping(target = "managerReviewerId", ignore = true)
    @Mapping(target = "managerDecisionNote", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(target = "financeUserId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Expense toEntity(ExpenseCreateDTO dto);

    // UpdateDTO -> existing Entity (only editable fields in DRAFT/REJECTED)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "project", source = "projectId", qualifiedByName = "toProjectRef")
    void updateEntity(@MappingTarget Expense entity, ExpenseUpdateDTO dto);

    // Entity -> ResponseDTO
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectCode", source = "project.code")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "projectGeneric", source = "project.generic")
    @Mapping(target = "allowedActions", ignore = true) // filled in service layer
    ExpenseResponseDTO toDTO(Expense entity);

    // Helper: map projectId to Project reference (id-only)
    @Named("toProjectRef")
    default Project toProjectRef(Long id) {
        if (id == null) return null;
        Project p = new Project();
        p.setId(id);
        return p;
    }
}


