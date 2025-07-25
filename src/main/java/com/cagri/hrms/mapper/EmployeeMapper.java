package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.request.employee.EmployeeCreateRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeResponseDTO;
import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.core.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {

    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "company.companyName", target = "companyName")
    @Mapping(source = "isPendingApprovalByManager", target = "isPendingApprovalByManager")
    EmployeeResponseDTO toDTO(Employee employee);

    @Mapping(target = "birthDate", source = "dto.birthDate")
    @Mapping(target = "hireDate", source = "dto.hireDate")
    @Mapping(target = "endDate", source = "dto.endDate")
    @Mapping(target = "position", source = "dto.position")
    @Mapping(target = "contractType", source = "dto.contractType")
    @Mapping(target = "phoneNumber", source = "dto.phoneNumber")
    @Mapping(target = "address", source = "dto.address")
    @Mapping(target = "salary", source = "dto.salary")
    @Mapping(target = "annualLeave", source = "dto.annualLeave")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", expression = "java(System.currentTimeMillis())")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "isPendingApprovalByManager", source = "dto.isPendingApprovalByManager")
    Employee toEntity(EmployeeCreateRequestDTO dto, @Context User user, @Context Company company);

    @AfterMapping
    default void assignUserAndCompany(@MappingTarget Employee employee,
                                      @Context User user,
                                      @Context Company company) {
        employee.setUser(user);
        employee.setCompany(company);
    }

    @Mapping(target = "updatedAt", expression = "java(System.currentTimeMillis())")
    @Mapping(target = "isPendingApprovalByManager", source = "dto.isPendingApprovalByManager")
    void updateFromDto(EmployeeCreateRequestDTO dto, @MappingTarget Employee employee);
}