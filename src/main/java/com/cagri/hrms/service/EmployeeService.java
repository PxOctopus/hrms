package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.employee.EmployeeCreateRequestDTO;
import com.cagri.hrms.dto.request.employee.EmployeeUpdateProfileRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeLiteDTO; // ⬅️ EKLENDİ
import com.cagri.hrms.dto.response.employee.EmployeeMeDTO;
import com.cagri.hrms.dto.response.employee.EmployeeResponseDTO;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.employee.Employee;

import java.util.List;
import java.util.Optional;

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

    // ---------------- ADDED: current employee & scoped getters ----------------
    /** Returns current employee id mapped from the authenticated user. */
    // ADDED:
    Long getCurrentEmployeeIdOrThrow();

    /** Returns employee by id and asserts it belongs to current company. */
    // ADDED:
    Employee getByIdScoped(Long employeeId);

    /** Returns employee by userId or throws; useful for company resolution. */
    // ADDED:
    Employee getByUserIdOrThrow(Long userId);

    Optional<Employee> findById(Long id);
}
