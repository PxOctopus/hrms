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
import com.cagri.hrms.service.AuthService;
import com.cagri.hrms.service.LeaveService;
import com.cagri.hrms.service.NotificationService;
import com.cagri.hrms.service.UserService;
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
    private final AuthService authService;
    private final UserService userService;

    /**
     * Submit a new leave request.
     * If a manager creates a leave for another employee, status is APPROVED by default.
     * If an employee creates their own leave request, status is PENDING.
     */
    @Override
    public void requestLeave(LeaveRequestDTO dto) {
        User currentUser = SecurityUtil.getCurrentUser();
        Employee targetEmployee;

        if (currentUser.hasRole("EMPLOYEE")) {
            // Employee can only request leave for themselves
            targetEmployee = employeeRepository.findByUser(currentUser)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            dto.setEmployeeId(targetEmployee.getId());
        } else if (currentUser.hasRole("MANAGER")) {
            // Manager must specify employeeId
            if (dto.getEmployeeId() == null) {
                throw new RuntimeException("Manager must provide employeeId");
            }

            targetEmployee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            // Manager should NOT be able to create leave for themselves
            if (targetEmployee.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Manager cannot request leave for themselves");
            }

            // Optional: Only allow leave request for employees in the same company
            if (!targetEmployee.getCompany().getId().equals(currentUser.getCompany().getId())) {
                throw new RuntimeException("You can only request leave for employees in your own company");
            }
        } else {
            throw new RuntimeException("Unauthorized role");
        }

        // Validate leave type
        if (dto.getLeaveDefinitionId() == null || dto.getLeaveDefinitionId() <= 0) {
            throw new RuntimeException("Leave type (leaveDefinitionId) must be selected.");
        }

        LeaveDefinition leaveDefinition = leaveDefinitionRepository.findById(dto.getLeaveDefinitionId())
                .orElseThrow(() -> new RuntimeException("Leave definition not found"));

        // Create and populate Leave entity
        Leave leave = leaveMapper.toEntity(dto);
        leave.setEmployee(targetEmployee);
        leave.setLeaveDefinition(leaveDefinition);
        leave.setRequestDate(LocalDate.now());

        // Track who created the leave (employee or manager)
        leave.setCreatedBy(currentUser);

        if (currentUser.hasRole("EMPLOYEE")) {
            leave.setStatus(LeaveStatus.PENDING);
        } else {
            leave.setStatus(LeaveStatus.APPROVED);
            leave.setDecisionDate(LocalDate.now());
            leave.setManagerNote("Approved by manager during creation");
            leave.setManager(currentUser);
        }

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

        // Set manager info (who approved or rejected)
        leave.setManager(SecurityUtil.getCurrentUser());

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

    @Override
    public List<LeaveResponseDTO> getLeavesAssignedByManager() {
        User currentManager = authService.getCurrentUser(); // manager who is logged in

        // Only leaves that this manager has created
        List<Leave> leaves = leaveRepository.findAllByCreatedBy_Id(currentManager.getId());

        return leaves.stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    @Override
    public List<LeaveResponseDTO> getLeavesWaitingForMyApproval() {
        User currentUser = userService.getCurrentUser();
        Long companyId = currentUser.getCompany().getId();

        List<Leave> pendingLeaves = leaveRepository
                .findByEmployee_Company_IdAndStatus(companyId, LeaveStatus.PENDING);

        return leaveMapper.toResponseDTOList(pendingLeaves);
    }
}
