package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.auth.LoginRequestDTO;
import com.cagri.hrms.dto.request.auth.RegisterRequestDTO;
import com.cagri.hrms.dto.response.auth.AuthResponseDTO;
import com.cagri.hrms.entity.Role;
import com.cagri.hrms.entity.User;
import com.cagri.hrms.repository.RoleRepository;
import com.cagri.hrms.repository.UserRepository;
import com.cagri.hrms.service.AuthService;
import com.cagri.hrms.service.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional // Runs DB operations atomically; rolls back on failure
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        Role defaultRole = roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(defaultRole)
                .emailVerified(false)
                .isActive(true)
                .createdAt(LocalDate.now())
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return new AuthResponseDTO(token);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal(); // CustomUserDetails varsa

        String token = jwtService.generateToken(user);

        return new AuthResponseDTO(token);
    }
}
