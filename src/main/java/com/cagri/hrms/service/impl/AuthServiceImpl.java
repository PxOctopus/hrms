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

    /**
     * Registers a new user (EMPLOYEE or MANAGER) into the system.
     * - If the user is an EMPLOYEE: associates with an existing company.
     * - If the user is a MANAGER: does not create a company but saves the intended company name as pending.
     * - Sends a verification email after registration.
     *
     * @param request The registration form data.
     * @return AuthResponseDTO containing JWT token and role name.
     * @throws BusinessException if email already exists, password mismatch, or company info is invalid.
     */
    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {

        // 1. Check if the email is already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email is already in use");
        }

        // 2. Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Password and confirmation do not match");
        }

        // 3. Determine user role or default to EMPLOYEE
        String roleName = Optional.ofNullable(request.getRoleName()).orElse("EMPLOYEE");

        // 4. Fetch role entity from the database
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new BusinessException("Role not found: " + roleName));

        // 5. Company will be determined if the user is an employee
        Company company = null;

        if ("EMPLOYEE".equalsIgnoreCase(roleName)) {
            // 5.1 Check that employee email domain matches company domain
            String expectedDomain = request.getCompanyEmail().split("@")[1];
            String userDomain = request.getEmail().split("@")[1];
            if (!userDomain.equalsIgnoreCase(expectedDomain)) {
                throw new BusinessException("Employee email must match company domain: @" + expectedDomain);
            }

            // 5.2 Fetch the existing company by name and email
            company = companyRepository.findByCompanyNameAndCompanyEmail(
                    request.getCompanyName(),
                    request.getCompanyEmail()
            ).orElseThrow(() -> new BusinessException("Company not found with provided name and email"));
        }

        // 6. Map the request to a User entity
        User user = authMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));           // Hash password
        user.setVerificationToken(UUID.randomUUID().toString());                   // Set email verification token
        user.setRole(role);                                                        // Set user role
        user.setCompany(company);                                                  // Null for MANAGERs
        user.setEnabled(false);                                                    // Disable account until email verification

        // 7. If manager, save company name as "pending" (company will be created later)
        if ("MANAGER".equalsIgnoreCase(roleName)) {
            user.setPendingCompanyName(request.getCompanyName());
        }

        // 8. Save user to DB
        userRepository.save(user);

        // 9. If EMPLOYEE, create and link Employee entity
        if ("EMPLOYEE".equalsIgnoreCase(roleName)) {
            Employee employee = new Employee();
            employee.setUser(user);
            employee.setCompany(company);
            employee.setHireDate(LocalDate.now());
            employee.setActive(true);
            employee.setCreatedAt(System.currentTimeMillis());
            employeeRepository.save(employee);
        }

        // 10. Send email verification link (customized for MANAGER)
        if ("MANAGER".equalsIgnoreCase(roleName)) {
            mailService.sendCompanyVerificationEmail(
                    request.getCompanyEmail(),
                    request.getFullName(),
                    request.getCompanyName(),
                    user.getVerificationToken()
            );
        } else {
            mailService.sendVerificationEmail(
                    request.getEmail(),
                    user.getVerificationToken()
            );
        }

        // 11. Generate JWT token and return response
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