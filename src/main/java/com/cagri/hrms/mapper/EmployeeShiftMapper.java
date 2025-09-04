package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.request.employee.EmployeeShiftRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeShiftResponseDTO;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.employee.EmployeeShift;
import com.cagri.hrms.entity.employee.Shift;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps between assignment entities and DTOs.
 * Note:
 *  - We pass Employee & Shift entities from the service to avoid ID->entity lookups in MapStruct.
 *  - Response DTO carries denormalized fields for easier rendering on the client.
 */
@Mapper(componentModel = "spring")
public interface EmployeeShiftMapper {

    // Entity -> DTO (denormalize employee/shift properties)
    @Mapping(target = "employeeId",   source = "employee.id")
    @Mapping(target = "employeeName", source = "employee.user.fullName")
    @Mapping(target = "shiftId",      source = "shift.id")
    @Mapping(target = "shiftName",    source = "shift.shiftName")
    EmployeeShiftResponseDTO toDto(EmployeeShift employeeShift);

    // DTO + hydrated entities -> Entity
    @Mapping(target = "id",       ignore = true)
    @Mapping(target = "employee", source = "employee")
    @Mapping(target = "shift",    source = "shift")
    @Mapping(target = "active",   source = "dto.active")
    EmployeeShift toEntity(EmployeeShiftRequestDTO dto, Employee employee, Shift shift);
}
