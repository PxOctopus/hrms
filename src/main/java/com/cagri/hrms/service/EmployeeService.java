package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.employee.EmployeeRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeResponseDTO;

import java.util.List;

public interface EmployeeService {

    EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO, Long userId, Long companyId);

    List<EmployeeResponseDTO> getAllEmployees();

    EmployeeResponseDTO getEmployeeById(Long id);

    EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO requestDTO);

    void deleteEmployee(Long id);
}
