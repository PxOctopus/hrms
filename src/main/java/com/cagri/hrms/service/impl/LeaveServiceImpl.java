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

    // Statuses that both block overlap and count against annual quota (enum names for native SQL)
    private static final List<String> BLOCKING_STATUSES =
            List.of(LeaveStatus.PENDING.name(), LeaveStatus.APPROVED.name());

    // ----------------------- small utilities -----------------------

    /** Inclusive day count (both start and end are counted). */
    private static int inclusiveDays(LocalDate start, LocalDate end) {
        return (int) (end.toEpochDay() - start.toEpochDay() + 1);
    }

    /** True if two date ranges overlap (inclusive). */
    private static boolean overlaps(LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {
        return !aStart.isAfter(bEnd) && !aEnd.isBefore(bStart);
    }

    /** Start of the calendar year for a given date. */
    private static LocalDate yearStart(LocalDate d) {
        return LocalDate.of(d.getYear(), 1, 1);
    }

    /** End of the calendar year for a given date. */
    private static LocalDate yearEnd(LocalDate d) {
        return LocalDate.of(d.getYear(), 12, 31);
    }

    /** Inclusive clipped days of [start,end] inside [winStart, winEnd]; returns 0 if no overlap. */
    private static int clippedInclusiveDays(LocalDate start, LocalDate end, LocalDate winStart, LocalDate winEnd) {
        if (!overlaps(start, end, winStart, winEnd)) return 0;
        LocalDate s = start.isBefore(winStart) ? winStart : start;
        LocalDate e = end.isAfter(winEnd) ? winEnd : end;
        return inclusiveDays(s, e);
    }

    // ----------------------- public checks (UI can call) -----------------------

    @Override
    public boolean hasOverlappingLeave(Long employeeId, LeaveRequestDTO dto) {
        if (dto.getStartDate() == null || dto.getEndDate() == null) return false;
        return leaveRepository.existsOverlappingLeave(
                employeeId,
                dto.getStartDate(),
                dto.getEndDate(),
                BLOCKING_STATUSES
        );
    }

    @Override
    public boolean hasRemainingAnnualLeave(Long employeeId, LeaveRequestDTO dto) {
        LeaveDefinition def = leaveDefinitionRepository.findById(dto.getLeaveDefinitionId())
                .orElseThrow(() -> new RuntimeException("Leave definition not found"));
        // Only annual leaves are quota-limited
        if (!def.isAnnual()) return true;

        LocalDate start = dto.getStartDate();
        LocalDate end   = dto.getEndDate();

        // Requested days (inclusive)
        int requested = inclusiveDays(start, end);

        // Sum previously used days clipped to the same year as the request's start
        LocalDate ws = yearStart(start);
        LocalDate we = yearEnd(start);

        Integer used = leaveRepository.getUsedLeaveDaysInWindow(
                employeeId,
                def.getId(),
                BLOCKING_STATUSES,
                ws, we
        );
        int alreadyUsed = used != null ? used : 0;
        int allowance   = def.getMaxDays() != null ? def.getMaxDays() : 0;

        return alreadyUsed + requested <= allowance;
    }

    // ----------------------- core flows -----------------------

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

            // Optional: only employees in the same company
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

        LocalDate start = dto.getStartDate();
        LocalDate end   = dto.getEndDate();

        // --- validate dates ---
        if (start == null || end == null) {
            throw new RuntimeException("Start and end dates must be provided.");
        }
        if (end.isBefore(start)) {
            throw new RuntimeException("End date cannot be earlier than start date.");
        }

        // --- overlap check ---
        boolean hasOverlap = leaveRepository.existsOverlappingLeave(
                targetEmployee.getId(),
                start,
                end,
                BLOCKING_STATUSES
        );
        if (hasOverlap) {
            throw new RuntimeException("Leave dates overlap with existing leave.");
        }

        // --- annual quota check (only for annual type) ---
        if (leaveDefinition.isAnnual()) {
            int requestedDays = inclusiveDays(start, end);
            LocalDate ws = yearStart(start);
            LocalDate we = yearEnd(start);

            Integer used = leaveRepository.getUsedLeaveDaysInWindow(
                    targetEmployee.getId(),
                    leaveDefinition.getId(),
                    BLOCKING_STATUSES,
                    ws, we
            );
            int usedDays  = used != null ? used : 0;
            int allowance = leaveDefinition.getMaxDays() != null ? leaveDefinition.getMaxDays() : 0;

            if (usedDays + requestedDays > allowance) {
                int remaining = Math.max(0, allowance - usedDays);
                throw new RuntimeException("Annual leave quota exceeded. Remaining days: " + remaining);
            }
        }

        // Create and populate Leave entity
        Leave leave = leaveMapper.toEntity(dto);
        leave.setEmployee(targetEmployee);
        leave.setLeaveDefinition(leaveDefinition);
        leave.setRequestDate(LocalDate.now());
        leave.setCreatedBy(currentUser);

        if (currentUser.hasRole("EMPLOYEE")) {
            leave.setStatus(LeaveStatus.PENDING);
        } else {
            // Manager-created leave is directly approved
            leave.setStatus(LeaveStatus.APPROVED);
            leave.setDecisionDate(LocalDate.now());
            leave.setManagerNote("Approved by manager during creation");
            leave.setManager(currentUser);
        }

        leaveRepository.save(leave);
    }

    /**
     * Approve or reject a leave request.
     * Re-validates overlap and annual quota on approval.
     * Notifies the employee about the decision.
     */
    @Override
    public void approveOrRejectLeave(LeaveApprovalDTO dto) {
        Leave leave = leaveRepository.findById(dto.getLeaveId())
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        // Basic date sanity
        if (leave.getStartDate() == null || leave.getEndDate() == null) {
            throw new RuntimeException("Leave has invalid dates.");
        }
        if (leave.getEndDate().isBefore(leave.getStartDate())) {
            throw new RuntimeException("Leave end date cannot be earlier than start date.");
        }

        // Assign manager who decides
        User actingManager = SecurityUtil.getCurrentUser();
        leave.setManager(actingManager);

        if (dto.isApproved()) {
            // --- Overlap check against OTHER leaves (exclude current leave) ---
            Employee emp = leave.getEmployee();
            List<Leave> allOfEmployee = leaveRepository.findByEmployee(emp);

            boolean overlapsAnother = allOfEmployee.stream()
                    .filter(l -> !l.getId().equals(leave.getId()))
                    .filter(l -> BLOCKING_STATUSES.contains(l.getStatus().name()))
                    .anyMatch(l -> overlaps(leave.getStartDate(), leave.getEndDate(), l.getStartDate(), l.getEndDate()));

            if (overlapsAnother) {
                throw new RuntimeException("Leave dates overlap with another leave of the employee.");
            }

            // --- Annual quota check on approval (exclude current pending from sum) ---
            LeaveDefinition def = leave.getLeaveDefinition();
            if (def != null && def.isAnnual()) {
                LocalDate start = leave.getStartDate();
                LocalDate end   = leave.getEndDate();

                LocalDate ws = yearStart(start);
                LocalDate we = yearEnd(start);

                // Sum used days (may include this PENDING leave)
                Integer used = leaveRepository.getUsedLeaveDaysInWindow(
                        emp.getId(), def.getId(), BLOCKING_STATUSES, ws, we
                );
                int usedTotal = used != null ? used : 0;

                // Clip current leave to window and remove it from 'used' if present
                int currentClipped = clippedInclusiveDays(start, end, ws, we);
                // If current leave is PENDING, it's part of the sum; subtract it to get "others used"
                int usedExcludingCurrent = leave.getStatus() == LeaveStatus.PENDING
                        ? Math.max(0, usedTotal - currentClipped)
                        : usedTotal;

                int allowance = def.getMaxDays() != null ? def.getMaxDays() : 0;

                if (usedExcludingCurrent + currentClipped > allowance) {
                    int remaining = Math.max(0, allowance - usedExcludingCurrent);
                    throw new RuntimeException("Annual leave quota exceeded. Remaining days: " + remaining);
                }
            }

            // Approve
            leave.setStatus(LeaveStatus.APPROVED);
            leave.setDecisionDate(LocalDate.now());

        } else {
            // Reject
            leave.setStatus(LeaveStatus.REJECTED);
            leave.setDecisionDate(LocalDate.now());
        }

        leaveRepository.save(leave);

        // Notify employee about the decision
        notificationService.sendLeaveDecisionNotification(leave.getEmployee(), dto.isApproved());
    }

    // ----------------------- queries -----------------------

    @Override
    public List<LeaveResponseDTO> getAllLeaves() {
        return leaveRepository.findAll().stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    @Override
    public List<LeaveResponseDTO> getLeavesByEmployeeId(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return leaveRepository.findByEmployee(employee).stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    @Override
    public List<LeaveResponseDTO> getLeavesOfCurrentEmployee() {
        User currentUser = SecurityUtil.getCurrentUser();
        Employee employee = employeeRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return leaveRepository.findByEmployee(employee).stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    @Override
    public List<LeaveResponseDTO> getApprovedLeaves() {
        return leaveRepository.findAllByStatus(LeaveStatus.APPROVED).stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    @Override
    public List<LeaveResponseDTO> getPendingLeaves() {
        return leaveRepository.findAllByStatus(LeaveStatus.PENDING).stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    @Override
    public List<LeaveResponseDTO> getRejectedLeaves() {
        return leaveRepository.findAllByStatus(LeaveStatus.REJECTED).stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    @Override
    public List<LeaveResponseDTO> getLeavesAssignedByManager() {
        User currentManager = authService.getCurrentUser();
        List<Leave> leaves = leaveRepository.findAllByCreatedBy_Id(currentManager.getId());
        return leaves.stream().map(leaveMapper::toDto).toList();
    }

    @Override
    public List<LeaveResponseDTO> getLeavesWaitingForMyApproval() {
        User currentUser = userService.getCurrentUser();
        Long companyId = currentUser.getCompany().getId();
        List<Leave> pendingLeaves =
                leaveRepository.findByEmployee_Company_IdAndStatus(companyId, LeaveStatus.PENDING);
        return leaveMapper.toResponseDTOList(pendingLeaves);
    }

    @Override
    public List<LeaveResponseDTO> getLeavesApprovedByManager() {
        User currentManager = authService.getCurrentUser();
        List<Leave> approvedLeaves =
                leaveRepository.findByStatusAndManager_Id(LeaveStatus.APPROVED, currentManager.getId());
        return approvedLeaves.stream().map(leaveMapper::toDto).toList();
    }
}
