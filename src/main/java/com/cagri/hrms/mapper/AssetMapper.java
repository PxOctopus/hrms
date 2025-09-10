package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.request.asset.AssetCreateRequestDTO;
import com.cagri.hrms.dto.request.asset.AssetUpdateRequestDTO;
import com.cagri.hrms.dto.response.asset.AssetEventResponseDTO;
import com.cagri.hrms.dto.response.asset.AssetMaintenanceResponseDTO;
import com.cagri.hrms.dto.response.employee.AssetResponseDTO;
import com.cagri.hrms.entity.asset.Asset;
import com.cagri.hrms.entity.asset.AssetEvent;
import com.cagri.hrms.entity.asset.AssetMaintenance;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AssetMapper {

    // Map Asset entity to AssetResponseDTO
    // Align field names: entity.assetName -> dto.name
    // Handle nested mapping for employee full name
    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "assetName"),
            @Mapping(target = "description", source = "description"),
            @Mapping(
                    target = "employeeFullName",
                    expression = "java(asset.getEmployee() != null && asset.getEmployee().getUser() != null "
                            + "? asset.getEmployee().getUser().getFullName() : null)"
            )
    })
    AssetResponseDTO toResponse(Asset asset);

    // Map AssetCreateRequestDTO to Asset entity (used on creation)
    @BeanMapping(ignoreByDefault = true)
    @Mappings({
            @Mapping(target = "assetName", source = "assetName"),
            @Mapping(target = "serialNumber", source = "serialNumber"),
            @Mapping(target = "category", source = "category"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "condition", source = "condition"),
            @Mapping(target = "location", source = "location")
    })
    Asset toEntity(AssetCreateRequestDTO dto);

    // Partial update: copy non-null fields from DTO to entity
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Asset asset, AssetUpdateRequestDTO dto);

    // Map AssetEvent entity to its response DTO
    AssetEventResponseDTO toResponse(AssetEvent event);

    // Map AssetMaintenance entity to its response DTO
    AssetMaintenanceResponseDTO toResponse(AssetMaintenance m);
}
