package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.response.role.RoleResponseDTO;
import com.cagri.hrms.entity.core.Role;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    RoleResponseDTO toDTO(Role role);
}
