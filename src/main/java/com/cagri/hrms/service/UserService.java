package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.user.UserRequestDTO;
import com.cagri.hrms.dto.response.user.UserResponseDTO;

import java.util.List;

public interface UserService {
    UserResponseDTO createUser(UserRequestDTO dto);
    UserResponseDTO getUserById(Long id);
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO updateUser(Long id, UserRequestDTO dto);
    void deleteUser(Long id);
}
