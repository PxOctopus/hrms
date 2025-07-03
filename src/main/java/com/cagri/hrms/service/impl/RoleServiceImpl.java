package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.response.role.RoleResponseDTO;
import com.cagri.hrms.mapper.RoleMapper;
import com.cagri.hrms.repository.RoleRepository;
import com.cagri.hrms.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    public List<RoleResponseDTO> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(roleMapper::toDTO) // Maps Role entity to RoleResponseDTO
                .collect(Collectors.toList());
    }
}
