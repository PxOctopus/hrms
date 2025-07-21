package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.employee.EmployeeShiftRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeShiftResponseDTO;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.employee.EmployeeShift;
import com.cagri.hrms.entity.employee.Shift;
import com.cagri.hrms.exception.ErrorType;
import com.cagri.hrms.exception.HrmsException;
import com.cagri.hrms.mapper.EmployeeShiftMapper;
import com.cagri.hrms.repository.EmployeeRepository;
import com.cagri.hrms.repository.EmployeeShiftRepository;
import com.cagri.hrms.repository.ShiftRepository;
import com.cagri.hrms.service.EmployeeShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeShiftServiceImpl implements EmployeeShiftService {

    private final EmployeeShiftRepository employeeShiftRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final EmployeeShiftMapper employeeShiftMapper;

    @Override
    public EmployeeShiftResponseDTO assignEmployeeShift(EmployeeShiftRequestDTO dto, User currentUser) {
        // Only MANAGER can assign shifts
        if (!currentUser.getRole().getName().equals("MANAGER")) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Only managers can assign shifts.");
        }
        // Check if the employee belongs to the same company as the manager
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "Employee not found"));
        if (!employee.getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Cannot assign shift to employee of another company.");
        }
        // Check if the shift is for the same company
        Shift shift = shiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "Shift not found"));
        if (!shift.getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Cannot assign another company's shift.");
        }
        EmployeeShift employeeShift = EmployeeShift.builder()
                .employee(employee)
                .shift(shift)
                .shiftDate(dto.getShiftDate())
                .active(true)
                .build();
        employeeShift = employeeShiftRepository.save(employeeShift);
        return employeeShiftMapper.toDto(employeeShift);
    }

    @Override
    public EmployeeShiftResponseDTO updateEmployeeShift(Long id, EmployeeShiftRequestDTO dto, User currentUser) {
        // Only MANAGER can update
        if (!currentUser.getRole().getName().equals("MANAGER")) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Only managers can update shifts.");
        }
        EmployeeShift employeeShift = employeeShiftRepository.findById(id)
                .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "EmployeeShift not found"));

        // Company consistency check
        if (!employeeShift.getEmployee().getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Cannot update another company's shift assignment.");
        }

        // Update employee if changed (and still same company)
        if (!employeeShift.getEmployee().getId().equals(dto.getEmployeeId())) {
            Employee employee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "Employee not found"));
            if (!employee.getCompany().getId().equals(currentUser.getCompany().getId())) {
                throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Employee does not belong to your company.");
            }
            employeeShift.setEmployee(employee);
        }
        // Update shift if changed (and still same company)
        if (!employeeShift.getShift().getId().equals(dto.getShiftId())) {
            Shift shift = shiftRepository.findById(dto.getShiftId())
                    .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "Shift not found"));
            if (!shift.getCompany().getId().equals(currentUser.getCompany().getId())) {
                throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Shift does not belong to your company.");
            }
            employeeShift.setShift(shift);
        }
        employeeShift.setShiftDate(dto.getShiftDate());
        employeeShift.setActive(dto.getActive());
        employeeShift = employeeShiftRepository.save(employeeShift);
        return employeeShiftMapper.toDto(employeeShift);
    }

    @Override
    public void deleteEmployeeShift(Long id, User currentUser) {
        // Only MANAGER can delete
        if (!currentUser.getRole().getName().equals("MANAGER")) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Only managers can delete shifts.");
        }
        EmployeeShift employeeShift = employeeShiftRepository.findById(id)
                .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "EmployeeShift not found"));
        // Check company
        if (!employeeShift.getEmployee().getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Cannot delete another company's shift assignment.");
        }
        // Soft delete
        employeeShift.setActive(false);
        employeeShiftRepository.save(employeeShift);
    }

    @Override
    public EmployeeShiftResponseDTO getEmployeeShiftById(Long id, User currentUser) {
        EmployeeShift employeeShift = employeeShiftRepository.findById(id)
                .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "EmployeeShift not found"));
        // Only MANAGER or the employee themselves
        boolean isManager = currentUser.getRole().getName().equals("MANAGER");
        boolean isSelf = employeeShift.getEmployee().getUser().getId().equals(currentUser.getId());
        if (!isManager && !isSelf) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Not authorized to view this shift assignment.");
        }
        return employeeShiftMapper.toDto(employeeShift);
    }

    @Override
    public List<EmployeeShiftResponseDTO> getEmployeeShiftsByEmployeeId(Long employeeId, User currentUser) {
        // Only MANAGER or the employee themselves
        boolean isManager = currentUser.getRole().getName().equals("MANAGER");
        boolean isSelf = employeeRepository.findByUserId(currentUser.getId())
                .map(employee -> employee.getId().equals(employeeId))
                .orElse(false);
        if (!isManager && !isSelf) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Not authorized to view these shift assignments.");
        }
        return employeeShiftRepository.findAllByEmployee_Id(employeeId)
                .stream()
                .map(employeeShiftMapper::toDto)
                .collect(Collectors.toList());
    }
}
