package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.response.expense.PayrollAdjustmentDTO;
import com.cagri.hrms.entity.expense.PayrollAdjustment;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayrollAdjustmentMapper {

    // Entity -> DTO
    @Mapping(target = "employeeId", source = "employee.id")
    PayrollAdjustmentDTO toDTO(PayrollAdjustment entity);

    // DTO -> Entity
    @InheritInverseConfiguration(name = "toDTO")
    @Mapping(target = "employee", ignore = true)
    PayrollAdjustment toEntity(PayrollAdjustmentDTO dto);
}
