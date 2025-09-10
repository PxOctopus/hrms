package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.response.employee.EmployeeAssetResponseDTO;
import com.cagri.hrms.entity.asset.Asset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeAssetMapper {

    @Mappings({
            @Mapping(target = "id",           source = "id"),
            @Mapping(target = "name",         source = "assetName"),
            @Mapping(target = "description",  source = "description"),
            @Mapping(target = "serialNumber", source = "serialNumber"),
            @Mapping(target = "status",       source = "status"),
            @Mapping(target = "confirmed",    source = "confirmed"),
            @Mapping(target = "assignedDate", source = "assignedDate")
    })
    EmployeeAssetResponseDTO toEmployeeDto(Asset asset);
}
