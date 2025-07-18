package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.employee.BreakRequestDTO;
import com.cagri.hrms.dto.response.employee.BreakResponseDTO;
import com.cagri.hrms.entity.employee.Break;
import com.cagri.hrms.entity.employee.Employee;
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
    public BreakResponseDTO createBreak(BreakRequestDTO requestDTO) {

        Employee employee = employeeRepository.findById(requestDTO.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));


        Break newBreak = breakMapper.toEntity(requestDTO);
        newBreak.setEmployee(employee);

        breakRepository.save(newBreak);

        return breakMapper.toDto(newBreak);
    }

    @Override
    public BreakResponseDTO updateBreak(Long id, BreakRequestDTO requestDTO) {
        // Fetches the existing break by ID
        Break existingBreak = breakRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Break not found"));

        // Updates the break details
        existingBreak.setBreakName(requestDTO.getBreakName());
        existingBreak.setStartTime(requestDTO.getStartTime());
        existingBreak.setEndTime(requestDTO.getEndTime());

        // If the employee is changed, reassign the break to the new employee
        if (!existingBreak.getEmployee().getId().equals(requestDTO.getEmployeeId())) {
            Employee employee = employeeRepository.findById(requestDTO.getEmployeeId())
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
            existingBreak.setEmployee(employee);
        }

        // Saves the updated break
        breakRepository.save(existingBreak);

        // Converts the updated entity to DTO and returns it
        return breakMapper.toDto(existingBreak);
    }

    @Override
    public void deleteBreak(Long id) {
        // Deletes a break by ID
        breakRepository.deleteById(id);
    }

    @Override
    public List<BreakResponseDTO> getAllBreaks() {
        // Fetches all breaks and converts them to a list of DTOs
        return breakRepository.findAll()
                .stream()
                .map(breakMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BreakResponseDTO> getBreaksByEmployeeId(Long employeeId) {
        // Fetches all breaks assigned to a specific employee and converts them to DTOs
        return breakRepository.findAllByEmployeeId(employeeId)
                .stream()
                .map(breakMapper::toDto)
                .collect(Collectors.toList());
    }
}
