package com.cagri.hrms.mapper;

import com.cagri.hrms.dto.request.user.UserRequestDTO;
import com.cagri.hrms.dto.response.user.UserResponseDTO;
import com.cagri.hrms.entity.core.Role;
import com.cagri.hrms.entity.core.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    // Maps User entity to UserResponseDTO:
// - Maps the nested role name from Role entity to a flat 'role' String field in the DTO.
// - Maps 'pendingCompanyName' from the User entity directly to the DTO,
//   which is used to show which company the manager has requested to create (admin view).
    @Mapping(source = "role.name", target = "role")
    @Mapping(source = "pendingCompanyName", target = "pendingCompanyName")
    UserResponseDTO toDTO(User user);

    // Converts UserRequestDTO to User entity using Role from @Context
    User toEntity(UserRequestDTO dto, @Context Role role);

    // Updates existing User entity with values from DTO and Role from context
    @Mapping(target = "id", ignore = true) // ID is managed manually
    void updateUserFromDto(UserRequestDTO dto, @MappingTarget User user, @Context Role role);
}

