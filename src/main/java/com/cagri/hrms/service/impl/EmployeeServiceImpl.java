package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.employee.EmployeeCreateRequestDTO;
import com.cagri.hrms.dto.request.employee.EmployeeUpdateProfileRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeLiteDTO; // ⬅️ EKLENDİ
import com.cagri.hrms.dto.response.employee.EmployeeMeDTO;
import com.cagri.hrms.dto.response.employee.EmployeeResponseDTO;
import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.core.Role;
import com.cagri.hrms.exception.ErrorType;
import com.cagri.hrms.exception.HrmsException;
import com.cagri.hrms.mapper.EmployeeMapper;
import com.cagri.hrms.repository.CompanyRepository;
import com.cagri.hrms.repository.EmployeeRepository;
import com.cagri.hrms.repository.UserRepository;
import com.cagri.hrms.repository.RoleRepository;
import com.cagri.hrms.service.*;
import com.cagri.hrms.util.PasswordUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    private final UserService userService;
    private final CompanyService companyService;

    @Override
    @Transactional(readOnly = true)
    public EmployeeMeDTO getMine(User currentUser) {
        boolean pending = employeeRepository.findByUserId(currentUser.getId())
                .map(e -> Boolean.TRUE.equals(e.getIsPendingApprovalByManager()))
                .orElse(true);
        return new EmployeeMeDTO(pending);
    }

    @Override
    public EmployeeResponseDTO createEmployee(EmployeeCreateRequestDTO requestDTO, User authenticatedUser) {
        Company company = authenticatedUser.getCompany();
        if (company == null) throw new EntityNotFoundException("Authenticated user is not associated with any company.");

        boolean isManager = authenticatedUser.getRole().getName().equals("MANAGER");
        String tempPassword = PasswordUtil.generateTempPassword(10);

        User employeeUser = new User();
        employeeUser.setFullName(requestDTO.getFullName());
        employeeUser.setEmail(requestDTO.getEmail());
        employeeUser.setPassword(passwordEncoder.encode(tempPassword));
        employeeUser.setMustChangePassword(true);
        employeeUser.setCompany(company);
        employeeUser.setRole(roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new EntityNotFoundException("Role 'EMPLOYEE' not found")));
        employeeUser.setEnabled(isManager);
        employeeUser.setEmailVerified(isManager);
        employeeUser.setIsActive(isManager);
        employeeUser.setCreatedAt(LocalDate.now());

        User savedUser = userRepository.save(employeeUser);

        Employee employee = employeeMapper.toEntity(requestDTO, savedUser, company);
        employee.setUser(savedUser);
        employee.setCompany(company);
        employee.setIsPendingApprovalByManager(!isManager);
        employee.setActive(!employee.getIsPendingApprovalByManager());

        employeeRepository.save(employee);

        if (employee.isActive()) {
            company.setNumberOfEmployees(company.getNumberOfEmployees() + 1);
            companyRepository.save(company);
        }
        if (isManager) {
            mailService.sendWelcomeEmail(requestDTO.getEmail(), requestDTO.getFullName(), tempPassword);
        }
        return employeeMapper.toDTO(employee);
    }

    @Override
    public List<EmployeeResponseDTO> getAllEmployees(User manager) {
        Long companyId = manager.getCompany().getId();
        return employeeRepository.findAllByCompanyId(companyId)
                .stream()
                .map(employeeMapper::toDTO)
                .toList();
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(Long id) {
        // Using NEW fetch-join METHOD
        Employee employee = employeeRepository.findByIdWithUserAndCompany(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + id));
        return employeeMapper.toDTO(employee);
    }

    @Transactional
    @Override
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeCreateRequestDTO requestDTO, User authenticatedUser) {
        Employee employee = employeeRepository.findByIdWithUserAndCompany(id) // ⬅️ fetch-join
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + id));

        Company company = authenticatedUser.getCompany();
        if (company == null) throw new EntityNotFoundException("Authenticated user is not associated with any company.");

        employeeMapper.updateFromDto(requestDTO, employee);

        User user = employee.getUser();
        if (user != null) {
            user.setFullName(requestDTO.getFullName());
            user.setEmail(requestDTO.getEmail());
            user.setPhoneNumber(requestDTO.getPhoneNumber());
            userRepository.save(user);
        }

        employee.setCompany(company);
        employee.setUpdatedAt(System.currentTimeMillis());

        boolean isManager = authenticatedUser.getRole().getName().equals("MANAGER");
        employee.setIsPendingApprovalByManager(!isManager);
        employee.setActive(!employee.getIsPendingApprovalByManager());

        Employee updated = employeeRepository.save(employee);
        return employeeMapper.toDTO(updated);
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + id));
        User user = employee.getUser();
        employeeRepository.delete(employee);
        userRepository.delete(user);
    }

    @Override
    public void approveEmployee(Long employeeId) {
        Employee employee = employeeRepository.findByIdWithUserAndCompany(employeeId) // ⬅️ fetch-join
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

        employee.setIsPendingApprovalByManager(false);
        employee.setActive(true);
        employee.setUpdatedAt(System.currentTimeMillis());

        User user = employee.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        employeeRepository.save(employee);

        Company company = employee.getCompany();
        company.setNumberOfEmployees(company.getNumberOfEmployees() + 1);
        companyRepository.save(company);

        mailService.sendApprovalEmail(user.getEmail(), company.getCompanyName());
    }

    @Override
    public void rejectEmployee(Long employeeId) {
        Employee employee = employeeRepository.findByIdWithUserAndCompany(employeeId) // ⬅️ fetch-join
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

        employee.setIsPendingApprovalByManager(false);
        employee.setActive(false);
        employee.setUpdatedAt(System.currentTimeMillis());

        User user = employee.getUser();
        user.setEnabled(false);
        userRepository.save(user);

        employeeRepository.save(employee);

        mailService.sendRejectionEmail(user.getEmail(), employee.getCompany().getCompanyName());
    }

    @Override
    public List<EmployeeResponseDTO> getPendingEmployeesForManager(User manager) {
        Long companyId = manager.getCompany().getId();
        return employeeRepository.findByCompanyIdAndIsPendingApprovalByManagerTrue(companyId)
                .stream()
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

        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setAddress(dto.getAddress());
        employee.setBirthDate(dto.getBirthDate());
        employee.setUpdatedAt(System.currentTimeMillis());

        User user = employee.getUser();
        user.setPhoneNumber(dto.getPhoneNumber());
        userRepository.save(user);

        employeeRepository.save(employee);
        return employeeMapper.toDTO(employee);
    }

    // NEW
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeLiteDTO> getAssignableEmployees(Long companyId) {
        return employeeRepository.findAssignable(companyId).stream()
                .map(e -> {
                    String fullName = e.getUser().getFullName();
                    String email = e.getUser().getEmail();
                    String display = (fullName == null || fullName.isBlank()) ? email : fullName;
                    return new EmployeeLiteDTO(e.getId(), display, email);
                })
                .toList();
    }

    // ---------------- ADDED: helpers ----------------

    @Override
    public Long getCurrentEmployeeIdOrThrow() {
        Long userId = userService.getCurrentUserId();
        Employee emp = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Employee profile not found"));
        return emp.getId();
    }

    @Override
    public Employee getByIdScoped(Long employeeId) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "Employee not found"));
        // row-level scope: make sure the employee belongs to the same company
        companyService.assertInCurrentCompany(emp.getCompany().getId());
        return emp;
    }

    @Override
    public Employee getByUserIdOrThrow(Long userId) {
        return employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "Employee not found by userId"));
    }

    @Override
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }
}
