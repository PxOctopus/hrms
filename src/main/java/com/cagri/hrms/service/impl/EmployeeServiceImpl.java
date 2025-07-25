package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.employee.EmployeeCreateRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeResponseDTO;
import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.mapper.EmployeeMapper;
import com.cagri.hrms.repository.EmployeeRepository;
import com.cagri.hrms.service.EmployeeService;
import com.cagri.hrms.service.MailService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final MailService mailService;

    @Override
    public EmployeeResponseDTO createEmployee(EmployeeCreateRequestDTO requestDTO, User authenticatedUser) {
        Company company = authenticatedUser.getCompany();
        if (company == null) {
            throw new EntityNotFoundException("Authenticated user is not associated with any company.");
        }

        Employee employee = employeeMapper.toEntity(requestDTO, authenticatedUser, company);

        // If manager didn't specify approval status, default to false (already approved)
        if (employee.getIsPendingApprovalByManager() == null) {
            employee.setIsPendingApprovalByManager(false);
        }

        // Default: set isActive based on approval status
        if (Boolean.TRUE.equals(employee.getIsPendingApprovalByManager())) {
            employee.setActive(false); // Awaiting approval
        } else {
            employee.setActive(true);  // Approved immediately
        }

        employeeRepository.save(employee);
        return employeeMapper.toDTO(employee);
    }

    @Override
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(employeeMapper::toDTO)
                .toList();
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + id));

        return employeeMapper.toDTO(employee);
    }

    @Override
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeCreateRequestDTO requestDTO, User authenticatedUser) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + id));

        Company company = authenticatedUser.getCompany();
        if (company == null) {
            throw new EntityNotFoundException("Authenticated user is not associated with any company.");
        }

        employeeMapper.updateFromDto(requestDTO, employee);

        employee.setUser(authenticatedUser);
        employee.setCompany(company);
        employee.setUpdatedAt(System.currentTimeMillis());

        // apply the same logic here too
        if (employee.getIsPendingApprovalByManager() == null) {
            employee.setIsPendingApprovalByManager(false);
        }

        if (Boolean.TRUE.equals(employee.getIsPendingApprovalByManager())) {
            employee.setActive(false);
        } else {
            employee.setActive(true);
        }

        employeeRepository.save(employee);
        return employeeMapper.toDTO(employee);
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + id));

        employeeRepository.delete(employee);
    }

    @Override
    public void approveEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

        employee.setIsPendingApprovalByManager(false); // No longer awaiting confirmation
        employee.setActive(true);                    // became active
        employee.setUpdatedAt(System.currentTimeMillis());

        employeeRepository.save(employee);

        // Send approval email to employee
        String email = employee.getUser().getEmail();
        String companyName = employee.getCompany().getCompanyName();
        mailService.sendApprovalEmail(email, companyName);
    }

    @Override
    public void rejectEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

        employee.setIsPendingApprovalByManager(false);
        employee.setActive(false); // left inactive because it is rejected
        employee.setUpdatedAt(System.currentTimeMillis());

        employeeRepository.save(employee);

        // Send rejection email to employee (optional)
        String email = employee.getUser().getEmail();
        String companyName = employee.getCompany().getCompanyName();
        mailService.sendRejectionEmail(email, companyName);
    }

    @Override
    public List<EmployeeResponseDTO> getPendingEmployeesForManager() {
        List<Employee> pendingEmployees = employeeRepository.findByIsPendingApprovalByManagerTrue();
        return pendingEmployees.stream()
                .map(employeeMapper::toDTO)
                .toList();
    }
}