package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.request.auth.RegisterRequestDTO;
import com.cagri.hrms.entity.Company;
import com.cagri.hrms.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    AuthMapper INSTANCE = Mappers.getMapper(AuthMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyManager", ignore = true)
    @Mapping(target = "numberOfEmployees", constant = "0")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "subscriptionPlan", ignore = true)
    @Mapping(target = "employeeLimitLevel", ignore = true)
    @Mapping(target = "employeeNumberLimit", ignore = true)
    @Mapping(target = "createAt", expression = "java(System.currentTimeMillis())")
    @Mapping(target = "updateAt", ignore = true)
    Company toCompany(RegisterRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDate.now())")
    @Mapping(target = "verificationToken", ignore = true) // set this manually
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "company", ignore = true)
    User toUser(RegisterRequestDTO dto);
}
