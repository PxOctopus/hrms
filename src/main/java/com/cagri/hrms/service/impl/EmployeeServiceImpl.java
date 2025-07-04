package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.employee.EmployeeRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeResponseDTO;
import com.cagri.hrms.entity.Company;
import com.cagri.hrms.entity.Employee;
import com.cagri.hrms.entity.User;
import com.cagri.hrms.mapper.EmployeeMapper;
import com.cagri.hrms.repository.CompanyRepository;
import com.cagri.hrms.repository.EmployeeRepository;
import com.cagri.hrms.repository.UserRepository;
import com.cagri.hrms.service.EmployeeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeMapper employeeMapper;


    @Override
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO) {
        User user = userRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + requestDTO.getUserId()));

        Company company = companyRepository.findById(requestDTO.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Company not found with ID: " + requestDTO.getCompanyId()));

        Employee employee = employeeMapper.toEntity(requestDTO, user, company);
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
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO requestDTO) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + id));

        employeeMapper.updateFromDto(requestDTO, employee); // Update with MapStruct
        employee.setUpdatedAt(System.currentTimeMillis());

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
