package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.employee.EmployeeCreateRequestDTO;
import com.cagri.hrms.dto.request.employee.EmployeeUpdateProfileRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeLiteDTO; // ⬅️ EKLENDİ
import com.cagri.hrms.dto.response.employee.EmployeeMeDTO;
import com.cagri.hrms.dto.response.employee.EmployeeResponseDTO;
import com.cagri.hrms.entity.core.User;

import java.util.List;

public interface EmployeeService {

    List<EmployeeResponseDTO> getAllEmployees(User manager);

    EmployeeResponseDTO getEmployeeById(Long id);

    void deleteEmployee(Long id);

    EmployeeResponseDTO createEmployee(EmployeeCreateRequestDTO dto, User authenticatedUser);

    EmployeeResponseDTO updateEmployee(Long id, EmployeeCreateRequestDTO dto, User user);

    void approveEmployee(Long employeeId);

    void rejectEmployee(Long employeeId);

    EmployeeResponseDTO toggleStatus(Long id);

    List<EmployeeResponseDTO> getPendingEmployeesForManager(User manager);

    EmployeeResponseDTO updateOwnProfile(Long userId, EmployeeUpdateProfileRequestDTO dto);

    EmployeeMeDTO getMine(User currentUser);

    // NEW
    List<EmployeeLiteDTO> getAssignableEmployees(Long companyId);
}
