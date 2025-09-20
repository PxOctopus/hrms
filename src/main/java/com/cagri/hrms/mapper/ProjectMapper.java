//package com.cagri.hrms.mapper;
//
//import com.cagri.hrms.dto.request.expense.ProjectCreateDTO;
//import com.cagri.hrms.dto.response.expense.ProjectResponseDTO;
//import com.cagri.hrms.entity.core.Company;
//import com.cagri.hrms.entity.expense.Project;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//
//@Mapper(componentModel = "spring")
//public interface ProjectMapper {
//
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "company", source = "company")
//    @Mapping(target = "active", constant = "true")
//    @Mapping(target = "isGeneric", source = "dto.generic")
//    @Mapping(target = "requiresAssignment", source = "dto.requiresAssignment")
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    Project toEntity(ProjectCreateDTO dto, Company company);
//
//    ProjectResponseDTO toDTO(Project entity);
//}
