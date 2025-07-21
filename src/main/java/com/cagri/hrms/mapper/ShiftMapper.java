package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.request.employee.ShiftRequestDTO;
import com.cagri.hrms.dto.response.employee.ShiftResponseDTO;
import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.entity.employee.Shift;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShiftMapper {
    ShiftResponseDTO toDto(Shift shift);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employeeShifts", ignore = true)
    @Mapping(target = "company", ignore = true)
    Shift toEntity(ShiftRequestDTO dto);
}
