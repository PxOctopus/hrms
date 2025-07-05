package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.user.ChangeEmailRequestDTO;
import com.cagri.hrms.dto.request.user.ChangePasswordRequestDTO;
import com.cagri.hrms.dto.request.user.UserRequestDTO;
import com.cagri.hrms.dto.response.user.UserResponseDTO;
import com.cagri.hrms.entity.User;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface UserService {
    UserResponseDTO createUser(UserRequestDTO dto);
    UserResponseDTO getUserById(Long id);
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO updateUser(Long id, UserRequestDTO dto);
    void deleteUser(Long id);
    void changePassword(Long id, ChangePasswordRequestDTO dto);
    void deactivateUser(Long id);
    void changeEmail(Long id, ChangeEmailRequestDTO dto);

    @NotNull(message = "User ID is required") Long getUserIdByEmail(String email);

    @NotNull(message = "Company ID is required") Long getCompanyIdByEmail(String email);

    User getUserByEmail(String email);


}
