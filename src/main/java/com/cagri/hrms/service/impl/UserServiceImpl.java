package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.user.ChangeEmailRequestDTO;
import com.cagri.hrms.dto.request.user.ChangePasswordRequestDTO;
import com.cagri.hrms.dto.request.user.UserRequestDTO;
import com.cagri.hrms.dto.response.user.UserResponseDTO;
import com.cagri.hrms.entity.core.Role;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.exception.BusinessException;
import com.cagri.hrms.mapper.UserMapper;
import com.cagri.hrms.repository.RoleRepository;
import com.cagri.hrms.repository.UserRepository;
import com.cagri.hrms.service.MailService;
import com.cagri.hrms.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.cagri.hrms.exception.ResourceNotFoundException;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
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

    @Override
    public void changePassword(Long id, ChangePasswordRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String encodedPassword = passwordEncoder.encode(dto.getNewPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    @Override
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // If the user to be deactivated is a MANAGER and there is only one active MANAGER in the system,
// prevent deactivation to ensure the system always has at least one active manager.
        if ("MANAGER".equalsIgnoreCase(user.getRole().getName()) &&
                userRepository.countByRoleNameAndEnabledTrue("MANAGER") <= 1) {
            throw new BusinessException("You cannot deactivate the only active manager in the system.");
        }

// Proceed to deactivate the user: disable login and mark the account as inactive.
        user.setIsActive(false);
        user.setEnabled(false); // Prevent login
        userRepository.save(user);
    }

    @Override
    public void changeEmail(Long id, ChangeEmailRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getNewEmail())) {
            throw new IllegalArgumentException("Email already in use: " + dto.getNewEmail());
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Set new email first
        user.setEmail(dto.getNewEmail());

        // Then generate a verification token and update the status
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setEmailVerified(false);
        user.setEnabled(false); // block login until verification

        userRepository.save(user);

        // Send to the updated (new) email address
        mailService.sendVerificationEmail(user.getEmail(), verificationToken);
    }

    @Override
    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email))
                .getId();
    }

    @Override
    public Long getCompanyIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getCompany() == null) {
            throw new ResourceNotFoundException("User is not associated with any company: " + email);
        }

        return user.getCompany().getId();
    }
}

