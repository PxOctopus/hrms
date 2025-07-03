package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.request.employee.EmployeeRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeResponseDTO;
import com.cagri.hrms.entity.Company;
import com.cagri.hrms.entity.Employee;
import com.cagri.hrms.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {

    // Maps fields from Employee entity to DTO
    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "company.companyName", target = "companyName")
    EmployeeResponseDTO toDTO(Employee employee);

    // Maps fields from DTO and binds context params manually (don't use source for context)
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
    @Mapping(target = "user", ignore = true)     // set manually in @AfterMapping
    @Mapping(target = "company", ignore = true)  // set manually in @AfterMapping
    Employee toEntity(EmployeeRequestDTO dto, @Context User user, @Context Company company);

    // Manually assign context parameters to the entity after mapping
    @org.mapstruct.AfterMapping
    default void assignUserAndCompany(@MappingTarget Employee employee,
                                      @Context User user,
                                      @Context Company company) {
        employee.setUser(user);
        employee.setCompany(company);
    }
}