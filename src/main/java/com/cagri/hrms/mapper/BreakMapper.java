package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.request.employee.BreakRequestDTO;
import com.cagri.hrms.dto.response.employee.BreakResponseDTO;
import com.cagri.hrms.entity.employee.Break;
import com.cagri.hrms.entity.employee.Employee;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BreakMapper {

    // Maps BreakRequestDTO to Break entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true) // ignore employee, set manually in service
    Break toEntity(BreakRequestDTO dto);

    // Maps Break entity to BreakResponseDTO
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeFullName", source = "employee.fullName")
    BreakResponseDTO toDto(Break breakEntity);
}
