package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.user.UserRequestDTO;
import com.cagri.hrms.dto.response.user.UserResponseDTO;
import com.cagri.hrms.entity.Role;
import com.cagri.hrms.entity.User;
import com.cagri.hrms.mapper.UserMapper;
import com.cagri.hrms.repository.RoleRepository;
import com.cagri.hrms.repository.UserRepository;
import com.cagri.hrms.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.cagri.hrms.exception.ResourceNotFoundException;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDTO createUser(UserRequestDTO dto) {
        // Retrieve Role entity by role name from DTO
        Role role = roleRepository.findByName(dto.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + dto.getRoleName()));

        // Map DTO to entity using Role as context
        User user = userMapper.toEntity(dto, role);
        return userMapper.toDTO(userRepository.save(user));
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toDTO(user);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Fetch a new role from DB and update fields via mapper
        Role role = roleRepository.findByName(dto.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + dto.getRoleName()));

        userMapper.updateUserFromDto(dto, existingUser, role);

        return userMapper.toDTO(userRepository.save(existingUser));
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }
}

