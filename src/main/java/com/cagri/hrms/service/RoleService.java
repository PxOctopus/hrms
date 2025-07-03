package com.cagri.hrms.service;

import com.cagri.hrms.dto.response.role.RoleResponseDTO;

import java.util.List;

public interface RoleService {
    List<RoleResponseDTO> getAllRoles();
}
