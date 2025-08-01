package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.employee.LeaveApprovalDTO;
import com.cagri.hrms.dto.request.employee.LeaveRequestDTO;
import com.cagri.hrms.dto.response.employee.LeaveResponseDTO;
import com.cagri.hrms.entity.core.LeaveDefinition;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.employee.Leave;
import com.cagri.hrms.enums.LeaveStatus;
import com.cagri.hrms.mapper.LeaveMapper;
import com.cagri.hrms.repository.EmployeeRepository;
import com.cagri.hrms.repository.LeaveDefinitionRepository;
import com.cagri.hrms.repository.LeaveRepository;
import com.cagri.hrms.service.LeaveService;
import com.cagri.hrms.service.NotificationService;
import com.cagri.hrms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveDefinitionRepository leaveDefinitionRepository;
    private final LeaveMapper leaveMapper;
    private final NotificationService notificationService;

    /**
     * Submit a new leave request.
     * If a manager creates a leave for another employee, status is APPROVED by default.
     * If an employee creates their own leave request, status is PENDING.
     */
    @Override
    public void requestLeave(LeaveRequestDTO dto) {
        Leave leave = leaveMapper.toEntity(dto);

        // Fetch employee and leave type from database
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        LeaveDefinition leaveDefinition = leaveDefinitionRepository.findById(dto.getLeaveDefinitionId())
                .orElseThrow(() -> new RuntimeException("Leave definition not found"));

        // Get current logged-in user
        User currentUser = SecurityUtil.getCurrentUser();

        // Check if the request is for the current user or for another employee
        boolean isSelfRequest = employee.getUser().getId().equals(currentUser.getId());

        // Security: Only managers can create leaves for other employees
        if (!isSelfRequest && !currentUser.hasRole("MANAGER")) {
            throw new RuntimeException("Only managers can create leave for others!");
            // Optionally, you can use: throw new AccessDeniedException(...)
        }

        // Set leave status and decision date depending on who is making the request
        if (isSelfRequest) {
            // Employee requests their own leave -> status is PENDING
            leave.setStatus(LeaveStatus.PENDING);
        } else {
            // Manager creates leave for an employee -> status is APPROVED
            leave.setStatus(LeaveStatus.APPROVED);
            leave.setDecisionDate(LocalDate.now());
            leave.setManagerNote("Created by manager");
        }

        // Set other fields
        leave.setEmployee(employee);
        leave.setLeaveDefinition(leaveDefinition);
        leave.setRequestDate(LocalDate.now());

        leaveRepository.save(leave);
    }

    /**
     * Approve or reject a leave request.
     * Updates the leave status and decision date.
     * Notifies the employee about the decision.
     */
    @Override
    public void approveOrRejectLeave(LeaveApprovalDTO dto) {
        Leave leave = leaveRepository.findById(dto.getLeaveId())
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        // Update leave status and decision date
        leave.setStatus(dto.isApproved() ? LeaveStatus.APPROVED : LeaveStatus.REJECTED);
        leave.setDecisionDate(LocalDate.now());

        leaveRepository.save(leave);

        // Notify employee about the decision (approve/reject)
        notificationService.sendLeaveDecisionNotification(leave.getEmployee(), dto.isApproved());
    }

    /**
     * Get all leaves in the system.
     */
    @Override
    public List<LeaveResponseDTO> getAllLeaves() {
        return leaveRepository.findAll().stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    /**
     * Get all leaves for a specific employee.
     */
    @Override
    public List<LeaveResponseDTO> getLeavesByEmployeeId(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return leaveRepository.findByEmployee(employee).stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    /**
     * Get all leaves of the current logged-in employee.
     */
    @Override
    public List<LeaveResponseDTO> getLeavesOfCurrentEmployee() {
        User currentUser = SecurityUtil.getCurrentUser();

        Employee employee = employeeRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return leaveRepository.findByEmployee(employee).stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    /**
     * Get all approved leaves.
     */
    @Override
    public List<LeaveResponseDTO> getApprovedLeaves() {
        return leaveRepository.findAllByStatus(LeaveStatus.APPROVED).stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    /**
     * Get all pending leaves.
     */
    @Override
    public List<LeaveResponseDTO> getPendingLeaves() {
        return leaveRepository.findAllByStatus(LeaveStatus.PENDING).stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    /**
     * Get all rejected leaves.
     */
    @Override
    public List<LeaveResponseDTO> getRejectedLeaves() {
        return leaveRepository.findAllByStatus(LeaveStatus.REJECTED).stream()
                .map(leaveMapper::toDto)
                .toList();
    }
}
