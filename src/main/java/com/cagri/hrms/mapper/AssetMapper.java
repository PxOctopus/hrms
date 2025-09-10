package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.request.asset.AssetCreateRequestDTO;
import com.cagri.hrms.dto.request.asset.AssetUpdateRequestDTO;
import com.cagri.hrms.dto.response.asset.AssetEventResponseDTO;
import com.cagri.hrms.dto.response.asset.AssetMaintenanceResponseDTO;
import com.cagri.hrms.dto.response.asset.AssetResponseDTO;
import com.cagri.hrms.entity.asset.Asset;
import com.cagri.hrms.entity.asset.AssetEvent;
import com.cagri.hrms.entity.asset.AssetMaintenance;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssetMapper {

    // CREATE
    @BeanMapping(ignoreByDefault = true)
    @Mappings({
            @Mapping(target = "assetName",    source = "assetName"),
            @Mapping(target = "serialNumber", source = "serialNumber"),
            @Mapping(target = "category",     source = "category"),
            @Mapping(target = "description",  source = "description"),
            @Mapping(target = "condition",    source = "condition"),
            @Mapping(target = "location",     source = "location"),
    })
    Asset toEntity(AssetCreateRequestDTO dto);

    // UPDATE (partial)
    @BeanMapping(ignoreByDefault = true,
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "assetName",   source = "assetName"),
            @Mapping(target = "category",    source = "category"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "condition",   source = "condition"),
            @Mapping(target = "location",    source = "location"),
    })
    void updateEntity(@MappingTarget Asset asset, AssetUpdateRequestDTO dto);

    // FULL DTO
    @Mappings({
            @Mapping(target = "id",           source = "id"),
            @Mapping(target = "assetName",    source = "assetName"),
            @Mapping(target = "serialNumber", source = "serialNumber"),
            @Mapping(target = "category",     source = "category"),
            @Mapping(target = "description",  source = "description"),
            @Mapping(target = "status",       source = "status"),
            @Mapping(target = "condition",    source = "condition"),
            @Mapping(target = "employeeId",   source = "employee.id"),
            @Mapping(target = "employeeName",
                    expression = "java(asset.getEmployee()!=null && asset.getEmployee().getUser()!=null ? asset.getEmployee().getUser().getFullName() : null)"),
            @Mapping(target = "managerId",    source = "manager.id"),
            @Mapping(target = "companyId",    source = "company.id"),
            @Mapping(target = "assignedDate", source = "assignedDate"),
            @Mapping(target = "confirmed",    source = "confirmed"),
            @Mapping(target = "location",     source = "location"),
            @Mapping(target = "createdAt",    source = "createdAt"),
            @Mapping(target = "updatedAt",    source = "updatedAt"),
    })
    AssetResponseDTO toResponse(Asset asset);

    // Event/Maintenance
    @Mappings({
            @Mapping(target = "id",           source = "id"),
            @Mapping(target = "type",         source = "type"),
            @Mapping(target = "metadataJson", source = "metadataJson"),
            @Mapping(target = "createdAt",    source = "createdAt"),
            @Mapping(target = "assetId",      source = "asset.id"),
            @Mapping(target = "actorUserId",  source = "actor.id")
    })
    AssetEventResponseDTO toResponse(AssetEvent event);

    @Mappings({
            @Mapping(target = "id",           source = "id"),
            @Mapping(target = "vendorName",   source = "vendorName"),
            @Mapping(target = "ticketNumber", source = "ticketNumber"),
            @Mapping(target = "status",       source = "status"),
            @Mapping(target = "notes",        source = "notes"),
            @Mapping(target = "openedDate",   source = "openedDate"),
            @Mapping(target = "closedDate",   source = "closedDate"),
            @Mapping(target = "assetId",      source = "asset.id")
    })
    AssetMaintenanceResponseDTO toResponse(AssetMaintenance maintenance);
}