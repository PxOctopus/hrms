package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.request.employee.AssetRequestDTO;
import com.cagri.hrms.dto.response.employee.AssetResponseDTO;
import com.cagri.hrms.entity.core.Asset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssetMapper {

    Asset toEntity(AssetRequestDTO dto);

    @Mapping(source = "employee.user.fullName", target = "employeeFullName")
    AssetResponseDTO toDto(Asset asset);
}
