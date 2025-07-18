package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.request.general.LeaveDefinitionRequestDTO;
import com.cagri.hrms.dto.response.general.LeaveDefinitionResponseDTO;
import com.cagri.hrms.entity.core.LeaveDefinition;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LeaveDefinitionMapper {
    LeaveDefinition toEntity(LeaveDefinitionRequestDTO dto);
    LeaveDefinitionResponseDTO toDto(LeaveDefinition entity);
}
