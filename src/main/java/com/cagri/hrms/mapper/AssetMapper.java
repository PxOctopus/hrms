package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.request.asset.AssetCreateRequestDTO;
import com.cagri.hrms.dto.request.asset.AssetUpdateRequestDTO;
import com.cagri.hrms.dto.request.employee.AssetRequestDTO;
import com.cagri.hrms.dto.response.asset.AssetEventResponseDTO;
import com.cagri.hrms.dto.response.asset.AssetMaintenanceResponseDTO;
import com.cagri.hrms.dto.response.employee.AssetResponseDTO;
import com.cagri.hrms.entity.asset.Asset;
import com.cagri.hrms.entity.asset.AssetEvent;
import com.cagri.hrms.entity.asset.AssetMaintenance;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AssetMapper {

    // Entity -> Response
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", expression = "java(asset.getEmployee() != null ? asset.getEmployee().getFullName() : null)")
    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(target = "companyId", source = "company.id")
    AssetResponseDTO toResponse(Asset asset);

    // CreateRequest -> Entity
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

    // UpdateRequest -> Entity (partial update)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Asset asset, AssetUpdateRequestDTO dto);

    // Events
    AssetEventResponseDTO toResponse(AssetEvent event);

    // Maintenance
    AssetMaintenanceResponseDTO toResponse(AssetMaintenance m);
}
