package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.employee.EmployeeCreateRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeResponseDTO;
import com.cagri.hrms.entity.User;

import java.util.List;

public interface EmployeeService {

    List<EmployeeResponseDTO> getAllEmployees();

    EmployeeResponseDTO getEmployeeById(Long id);

    void deleteEmployee(Long id);

    EmployeeResponseDTO createEmployee(EmployeeCreateRequestDTO dto, User authenticatedUser);

    EmployeeResponseDTO updateEmployee(Long id, EmployeeCreateRequestDTO dto, User user);
}