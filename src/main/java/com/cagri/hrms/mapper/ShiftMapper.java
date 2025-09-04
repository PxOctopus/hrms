package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.request.employee.ShiftRequestDTO;
import com.cagri.hrms.dto.response.employee.ShiftResponseDTO;
import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.entity.employee.Shift;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps between Shift entity and its DTOs.
 * Important:
 *  - company is ignored when mapping to entity; set it explicitly in the service
 *    from the authenticated user's company to prevent cross-company leakage.
 */
@Mapper(componentModel = "spring")
public interface ShiftMapper {

    // Entity -> DTO (expose companyId to the client)
    @Mapping(target = "companyId", source = "company.id")
    ShiftResponseDTO toDto(Shift shift);

    // DTO -> Entity (company set in service layer)
    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "employeeShifts",  ignore = true)
    @Mapping(target = "company",         ignore = true)
    Shift toEntity(ShiftRequestDTO dto);
}
