package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.employee.EmployeeCreateRequestDTO;
import com.cagri.hrms.dto.request.employee.EmployeeUpdateProfileRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeResponseDTO;
import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.core.Role;
import com.cagri.hrms.mapper.EmployeeMapper;
import com.cagri.hrms.repository.CompanyRepository;
import com.cagri.hrms.repository.EmployeeRepository;
import com.cagri.hrms.repository.UserRepository;
import com.cagri.hrms.repository.RoleRepository;
import com.cagri.hrms.service.EmployeeService;
import com.cagri.hrms.service.MailService;
import com.cagri.hrms.util.PasswordUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final MailService mailService;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public EmployeeResponseDTO createEmployee(EmployeeCreateRequestDTO requestDTO, User authenticatedUser) {
        // Get the company associated with the authenticated manager
        Company company = authenticatedUser.getCompany();
        if (company == null) {
            throw new EntityNotFoundException("Authenticated user is not associated with any company.");
        }

        // Check if the authenticated user is a manager
        boolean isManager = authenticatedUser.getRole().getName().equals("MANAGER");

        // Generate a temporary password for the new employee
        String tempPassword = PasswordUtil.generateTempPassword(10);

        // Create a new User entity for the employee
        User employeeUser = new User();
        employeeUser.setFullName(requestDTO.getFullName());
        employeeUser.setEmail(requestDTO.getEmail());
        employeeUser.setPassword(passwordEncoder.encode(tempPassword));
        employeeUser.setMustChangePassword(true); // Force password change on first login
        employeeUser.setCompany(company); // Associate user with the same company as the manager
        employeeUser.setRole(roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new EntityNotFoundException("Role 'EMPLOYEE' not found")));
        employeeUser.setEnabled(isManager); // Enable user immediately if created by a manager
        employeeUser.setEmailVerified(isManager); // Auto-verify email if created by a manager
        employeeUser.setIsActive(isManager); // Mark user as active only if created by a manager (bypasses approval)
        employeeUser.setCreatedAt(LocalDate.now());

        // Save and use returned savedUser to ensure user ID is populated
        User savedUser = userRepository.save(employeeUser);

        // Map request DTO to Employee entity and a link saved user and company
        Employee employee = employeeMapper.toEntity(requestDTO, savedUser, company);
        employee.setUser(savedUser); // Ensuring employee.user_id is set by explicitly assigning savedUser in this method
        employee.setCompany(company); // Ensure company is explicitly set
        employee.setIsPendingApprovalByManager(!isManager); // Require approval if not created by manager
        employee.setActive(!employee.getIsPendingApprovalByManager()); // Active only if approved

        // Save the Employee entity
        employeeRepository.save(employee);

        // If the employee is active immediately, increment the company's employee count
        if (employee.isActive()) {
            company.setNumberOfEmployees(company.getNumberOfEmployees() + 1);
            companyRepository.save(company);
        }

        // Send welcome email if employee is created directly by the manager
        if (isManager) {
            mailService.sendWelcomeEmail(
                    requestDTO.getEmail(),
                    requestDTO.getFullName(),
                    tempPassword
            );
        }

        // Convert and return the Employee entity as a response DTO
        return employeeMapper.toDTO(employee);
    }

    @Override
    public List<EmployeeResponseDTO> getAllEmployees(User manager) {
        Long companyId = manager.getCompany().getId();
        // Fetch all employees in the manager's company and map to DTOs
        return employeeRepository.findAllByCompanyId(companyId)
                .stream()
                .map(employeeMapper::toDTO)
                .toList();
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + id));
        // Map Employee entity to DTO
        return employeeMapper.toDTO(employee);
    }

    @Transactional
    @Override
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeCreateRequestDTO requestDTO, User authenticatedUser) {
        // 1. Fetch the employee entity
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + id));

        // 2. Ensure the authenticated user is associated with a company
        Company company = authenticatedUser.getCompany();
        if (company == null) {
            throw new EntityNotFoundException("Authenticated user is not associated with any company.");
        }

        // 3. Update employee fields using MapStruct
        employeeMapper.updateFromDto(requestDTO, employee);

        // 4. Update associated User entity
        User user = employee.getUser();
        if (user != null) {
            user.setFullName(requestDTO.getFullName());
            user.setEmail(requestDTO.getEmail());
            user.setPhoneNumber(requestDTO.getPhoneNumber());
            userRepository.save(user);
        }

        // 5. Set company relation and update timestamp
        employee.setCompany(company);
        employee.setUpdatedAt(System.currentTimeMillis());

        // 6. Determine approval status based on the authenticated user's role
        boolean isManager = authenticatedUser.getRole().getName().equals("MANAGER");
        employee.setIsPendingApprovalByManager(!isManager);

        // 7. Automatically set active status based on approval status
        employee.setActive(!employee.getIsPendingApprovalByManager());

        // 8. Save and return the updated DTO
        Employee updated = employeeRepository.save(employee);
        return employeeMapper.toDTO(updated);
    }
    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + id));

        User user = employee.getUser(); // get the user connected to the employee

        // Remove employee from the database
        employeeRepository.delete(employee);
         // Remove user from the database
        userRepository.delete(user);
    }

    @Override
    public void approveEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

        // Set employee as approved and active
        employee.setIsPendingApprovalByManager(false);
        employee.setActive(true);
        employee.setUpdatedAt(System.currentTimeMillis());

        // Enable associated user so the employee can log in
        User user = employee.getUser();
        user.setEnabled(true); // <-- This is required for login!
        userRepository.save(user);

        employeeRepository.save(employee);

        // Update company's employee count
        Company company = employee.getCompany();
        company.setNumberOfEmployees(company.getNumberOfEmployees() + 1);
        companyRepository.save(company);

        // Send approval notification to the employee
        String email = user.getEmail();
        String companyName = company.getCompanyName();
        mailService.sendApprovalEmail(email, companyName);
    }

    @Override
    public void rejectEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

        // Mark employee as rejected and inactive
        employee.setIsPendingApprovalByManager(false);
        employee.setActive(false); // Employee cannot work in the company
        employee.setUpdatedAt(System.currentTimeMillis());

        // Also disable user's login here if desired
        User user = employee.getUser();
        user.setEnabled(false);
        userRepository.save(user);

        employeeRepository.save(employee);

        // Send rejection notification to the employee
        String email = user.getEmail();
        String companyName = employee.getCompany().getCompanyName();
        mailService.sendRejectionEmail(email, companyName);
    }

    @Override
    public List<EmployeeResponseDTO> getPendingEmployeesForManager(User manager) {
        Long companyId = manager.getCompany().getId();
        // Retrieve employees who are waiting for manager approval
        List<Employee> pendingEmployees =
                employeeRepository.findByCompanyIdAndIsPendingApprovalByManagerTrue(companyId);

        return pendingEmployees.stream()
                .map(employeeMapper::toDTO)
                .toList();
    }

    @Override
    public EmployeeResponseDTO toggleStatus(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + id));

        employee.setActive(!employee.isActive());
        employee.setUpdatedAt(System.currentTimeMillis());

        Employee updated = employeeRepository.save(employee);

        return employeeMapper.toDTO(updated);
    }

    @Override
    public EmployeeResponseDTO updateOwnProfile(Long userId, EmployeeUpdateProfileRequestDTO dto) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found for user ID: " + userId));

        // Update employee-specific fields
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setAddress(dto.getAddress());
        employee.setBirthDate(dto.getBirthDate());
        employee.setUpdatedAt(System.currentTimeMillis());

        // Update user phone number too
        User user = employee.getUser();
        user.setPhoneNumber(dto.getPhoneNumber());
        userRepository.save(user); // 🔒 Explicit save

        employeeRepository.save(employee);
        return employeeMapper.toDTO(employee);
    }
}
