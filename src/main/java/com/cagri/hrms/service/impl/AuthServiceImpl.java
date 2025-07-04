package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.auth.LoginRequestDTO;
import com.cagri.hrms.dto.request.auth.RegisterRequestDTO;
import com.cagri.hrms.dto.request.general.ForgotPasswordRequestDTO;
import com.cagri.hrms.dto.request.general.ResetPasswordRequestDTO;
import com.cagri.hrms.dto.request.user.VerifyEmailRequestDTO;
import com.cagri.hrms.dto.response.auth.AuthResponseDTO;
import com.cagri.hrms.entity.Role;
import com.cagri.hrms.entity.User;
import com.cagri.hrms.repository.RoleRepository;
import com.cagri.hrms.repository.UserRepository;
import com.cagri.hrms.service.AuthService;
import com.cagri.hrms.service.JwtService;
import com.cagri.hrms.service.MailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional // Ensures all DB operations within a method are atomic and rolled back on failure
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MailService mailService;

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {
        // Check if the email is already used
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        // Load default role for new users (usually EMPLOYEE)
        Role defaultRole = roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        // Generate email verification token
        String verificationToken = UUID.randomUUID().toString();

        // Build the user entity
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(defaultRole)
                .emailVerified(false)
                .isActive(true)
                .createdAt(LocalDate.now())
                .verificationToken(verificationToken)
                .build();

        // Save the user in the database
        userRepository.save(user);

        // Send the verification email
        mailService.sendVerificationEmail(user.getEmail(), verificationToken);

        // Generate JWT token for login response
        String token = jwtService.generateToken(user);

        return new AuthResponseDTO(token);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        // Authenticate the user using Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Cast to your CustomUserDetails or use directly if User implements UserDetails
        User user = (User) authentication.getPrincipal();

        // Generate JWT token
        String token = jwtService.generateToken(user);

        return new AuthResponseDTO(token);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDate.now().plusDays(1)); // 1 gün geçerli

        userRepository.save(user);

        mailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }


    @Override
    public void resetPassword(ResetPasswordRequestDTO request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDate.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
    }

    @Override
    public void verifyEmail(VerifyEmailRequestDTO request) {
        User user = userRepository.findByVerificationToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        user.setEmailVerified(true);
        user.setVerificationToken(null);

        userRepository.save(user);
    }
}