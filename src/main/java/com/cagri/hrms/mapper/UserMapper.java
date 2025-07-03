package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.request.user.UserRequestDTO;
import com.cagri.hrms.dto.response.user.UserResponseDTO;
import com.cagri.hrms.entity.Role;
import com.cagri.hrms.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    // Maps User entity to UserResponseDTO and extracts the role name as String
    @Mapping(source = "role.name", target = "role")
    UserResponseDTO toDTO(User user);

    // Converts UserRequestDTO to User entity using Role from @Context
    User toEntity(UserRequestDTO dto, @Context Role role);

    // Updates existing User entity with values from DTO and Role from context
    @Mapping(target = "id", ignore = true) // ID is managed manually
    void updateUserFromDto(UserRequestDTO dto, @MappingTarget User user, @Context Role role);
}

