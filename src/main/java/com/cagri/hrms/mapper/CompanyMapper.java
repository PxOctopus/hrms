package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.request.company.CompanyRequestDTO;
import com.cagri.hrms.dto.response.company.CompanyResponseDTO;
import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.entity.core.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompanyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numberOfEmployees", constant = "0")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createAt", expression = "java(System.currentTimeMillis())")
    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "companyManager", source = "manager")
    Company toEntity(CompanyRequestDTO dto, User manager);

    @Mapping(target = "managerFullName", source = "companyManager.fullName")
    CompanyResponseDTO toDTO(Company company);

}
