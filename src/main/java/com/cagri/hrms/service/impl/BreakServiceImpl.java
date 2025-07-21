package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.employee.BreakRequestDTO;
import com.cagri.hrms.dto.response.employee.BreakResponseDTO;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.employee.Break;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.exception.ErrorType;
import com.cagri.hrms.exception.HrmsException;
import com.cagri.hrms.mapper.BreakMapper;
import com.cagri.hrms.repository.BreakRepository;
import com.cagri.hrms.repository.EmployeeRepository;
import com.cagri.hrms.service.BreakService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BreakServiceImpl implements BreakService {

    private final BreakRepository breakRepository;
    private final EmployeeRepository employeeRepository;
    private final BreakMapper breakMapper;

    @Override
    public BreakResponseDTO createBreak(BreakRequestDTO requestDTO, User currentUser) {
        // Only MANAGER can create a break
        if (!currentUser.getRole().getName().equals("MANAGER")) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Not authorized to create breaks.");
        }
        // Finds the employee for the break
        Employee employee = employeeRepository.findById(requestDTO.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        Break newBreak = breakMapper.toEntity(requestDTO);
        newBreak.setEmployee(employee);
        breakRepository.save(newBreak);

        return breakMapper.toDto(newBreak);
    }

    @Override
    public BreakResponseDTO updateBreak(Long id, BreakRequestDTO requestDTO, User currentUser) {
        // Only MANAGER can update a break
        if (!currentUser.getRole().getName().equals("MANAGER")) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Not authorized to update breaks.");
        }
        // Finds the existing break by ID
        Break existingBreak = breakRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Break not found"));

        // Updates break fields
        existingBreak.setBreakName(requestDTO.getBreakName());
        existingBreak.setStartTime(requestDTO.getStartTime());
        existingBreak.setEndTime(requestDTO.getEndTime());

        // If employee changed, update employee
        if (!existingBreak.getEmployee().getId().equals(requestDTO.getEmployeeId())) {
            Employee employee = employeeRepository.findById(requestDTO.getEmployeeId())
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
            existingBreak.setEmployee(employee);
        }

        breakRepository.save(existingBreak);
        return breakMapper.toDto(existingBreak);
    }

    @Override
    public void deleteBreak(Long id, User currentUser) {
        // Only MANAGER can delete a break
        if (!currentUser.getRole().getName().equals("MANAGER")) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Not authorized to delete breaks.");
        }
        // Deletes a break by its ID
        breakRepository.deleteById(id);
    }

    @Override
    public List<BreakResponseDTO> getAllBreaks(User currentUser) {
        // Only MANAGER can view all breaks
        if (!currentUser.getRole().getName().equals("MANAGER")) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Not authorized to view all breaks.");
        }
        return breakRepository.findAll()
                .stream()
                .map(breakMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BreakResponseDTO> getBreaksByEmployeeId(Long employeeId, User currentUser) {
        // Check if the user has MANAGER role
        boolean isManager = currentUser.getRole().getName().equals("MANAGER");

        // Check if the user is trying to access their own breaks (for EMPLOYEE)
        boolean isSelf = employeeRepository.findByUserId(currentUser.getId())
                .map(employee -> employee.getId().equals(employeeId))
                .orElse(false);

        // If the user is neither a manager nor the owner of the breaks, deny access
        if (!isManager && !isSelf) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Not authorized to view this employee's breaks.");
        }

        // Fetch and return all breaks for the specified employee
        return breakRepository.findAllByEmployeeId(employeeId)
                .stream()
                .map(breakMapper::toDto)
                .collect(Collectors.toList());
    }
}