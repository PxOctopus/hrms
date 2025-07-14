package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.employee.EmployeeCreateRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeResponseDTO;
import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.mapper.EmployeeMapper;
import com.cagri.hrms.repository.EmployeeRepository;
import com.cagri.hrms.service.EmployeeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    public EmployeeResponseDTO createEmployee(EmployeeCreateRequestDTO requestDTO, User authenticatedUser) {
        // Get associated company from the authenticated user
        Company company = authenticatedUser.getCompany();
        if (company == null) {
            throw new EntityNotFoundException("Authenticated user is not associated with any company.");
        }

        // Map DTO to Employee entity using MapStruct
        Employee employee = employeeMapper.toEntity(requestDTO, authenticatedUser, company);

        // Save employee to the database
        employeeRepository.save(employee);

        // Map saved employee to response DTO
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
        // Fetch existing employee
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + id));

        // Check company association
        Company company = authenticatedUser.getCompany();
        if (company == null) {
            throw new EntityNotFoundException("Authenticated user is not associated with any company.");
        }

        // Update fields from DTO
        employeeMapper.updateFromDto(requestDTO, employee);

        // (Optional) Update user and company associations if needed
        employee.setUser(authenticatedUser);
        employee.setCompany(company);

        employee.setUpdatedAt(System.currentTimeMillis());

        // Save updated employee
        employeeRepository.save(employee);

        return employeeMapper.toDTO(employee);
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + id));

        employeeRepository.delete(employee);
    }
}