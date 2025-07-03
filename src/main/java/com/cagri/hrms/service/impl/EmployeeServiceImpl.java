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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId()).orElseThrow();
        Company company = companyRepository.findById(dto.getCompanyId()).orElseThrow();

        Employee employee = employeeMapper.toEntity(dto, user, company);
        employeeRepository.save(employee);

        return employeeMapper.toDTO(employee);
    }

    @Override
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO, Long userId, Long companyId) {
        return null;
    }

    @Override
    public List<EmployeeResponseDTO> getAllEmployees() {
        return List.of();
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(Long id) {
        return null;
    }

    @Override
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO requestDTO) {
        return null;
    }

    @Override
    public void deleteEmployee(Long id) {

    }
}
