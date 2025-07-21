package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.employee.EmployeeShiftRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeShiftResponseDTO;
import com.cagri.hrms.entity.core.User;

import java.util.List;

public interface EmployeeShiftService {
    EmployeeShiftResponseDTO assignEmployeeShift(EmployeeShiftRequestDTO request, User currentUser);
    EmployeeShiftResponseDTO updateEmployeeShift(Long id, EmployeeShiftRequestDTO request, User currentUser);
    void deleteEmployeeShift(Long id, User currentUser);
    EmployeeShiftResponseDTO getEmployeeShiftById(Long id, User currentUser);
    List<EmployeeShiftResponseDTO> getEmployeeShiftsByEmployeeId(Long employeeId, User currentUser);
}