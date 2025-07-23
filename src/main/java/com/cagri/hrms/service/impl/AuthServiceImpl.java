package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.auth.LoginRequestDTO;
import com.cagri.hrms.dto.request.auth.RegisterRequestDTO;
import com.cagri.hrms.dto.request.general.ForgotPasswordRequestDTO;
import com.cagri.hrms.dto.request.general.ResetPasswordRequestDTO;
import com.cagri.hrms.dto.request.user.VerifyEmailRequestDTO;
import com.cagri.hrms.dto.response.auth.AuthResponseDTO;
import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.core.Role;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.exception.BusinessException;
import com.cagri.hrms.mapper.AuthMapper;
import com.cagri.hrms.repository.CompanyRepository;
import com.cagri.hrms.repository.EmployeeRepository;
import com.cagri.hrms.repository.RoleRepository;
import com.cagri.hrms.repository.UserRepository;
import com.cagri.hrms.security.CustomUserDetails;
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
import java.util.Optional;
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
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final AuthMapper authMapper;

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {
        // Check if email is already in use
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email is already in use");
        }

        // Check if password and confirmPassword match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Password and confirmation do not match");
        }

        // Determine role name or default to EMPLOYEE
        String roleName = Optional.ofNullable(request.getRoleName()).orElse("EMPLOYEE");

        // Fetch role from database
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new BusinessException("Role not found: " + roleName));

        // If employee, enforce email domain match with company
        if ("EMPLOYEE".equalsIgnoreCase(roleName)) {
            String expectedDomain = request.getCompanyEmail().split("@")[1];
            String userDomain = request.getEmail().split("@")[1];
            if (!userDomain.equalsIgnoreCase(expectedDomain)) {
                throw new BusinessException("Employee email must match company domain: @" + expectedDomain);
            }
        }

        // Initialize company as null (in case role is EMPLOYEE and company already exists)
        Company company;

        // If role is MANAGER, create a new company
        if ("MANAGER".equalsIgnoreCase(roleName)) {
            // Map and save new company from request
            company = authMapper.toCompany(request);
            companyRepository.save(company);
        } else {
            // Find existing company by name and email
            company = companyRepository.findByCompanyNameAndCompanyEmail(
                    request.getCompanyName(),
                    request.getCompanyEmail()
            ).orElseThrow(() -> new BusinessException("Company not found with provided name and email"));
        }

        // Map request to User entity
        User user = authMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setRole(role);
        user.setCompany(company);
        user.setEnabled(false); // User is initially disabled until email verification


        // If the user is a manager, also set them as companyManager
        if ("MANAGER".equalsIgnoreCase(roleName)) {
            company.setCompanyManager(user); // Set user as manager
            companyRepository.save(company);
        }

        // Save user
        userRepository.save(user);

        // If the user is an employee, create an Employee record
        if ("EMPLOYEE".equalsIgnoreCase(roleName)) {
            Employee employee = new Employee();
            employee.setUser(user);
            employee.setCompany(company);
            employee.setHireDate(LocalDate.now());
            employee.setActive(true);
            employee.setCreatedAt(System.currentTimeMillis());
            employeeRepository.save(employee);
        }

        // Send verification email based on role
        if ("MANAGER".equalsIgnoreCase(roleName)) {
            mailService.sendCompanyVerificationEmail(
                    request.getCompanyEmail(),
                    user.getFullName(),
                    company.getCompanyName(),
                    user.getVerificationToken()
            );
        } else {
            mailService.sendVerificationEmail(
                    request.getEmail(),
                    user.getVerificationToken()
            );
        }

        // Generate and return JWT token
        String token = jwtService.generateToken(user);
        return new AuthResponseDTO(token, user.getRole().getName());
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

        // Cast to CustomUserDetails
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // Generate JWT token
        String token = jwtService.generateToken(user);

        return new AuthResponseDTO(token, user.getRole().getName());
    }

    @Override
    public void forgotPassword(ForgotPasswordRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDate.now().plusDays(1)); //

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
                .orElseThrow(() -> new BusinessException("Invalid verification token"));
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BusinessException("Account already verified.");
        }
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setVerificationToken(null);

        userRepository.save(user);
    }
}