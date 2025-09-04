package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.employee.LeaveApprovalDTO;
import com.cagri.hrms.dto.request.employee.LeaveCheckDTO;
import com.cagri.hrms.dto.request.employee.LeaveRequestDTO;
import com.cagri.hrms.dto.response.employee.LeaveResponseDTO;
import com.cagri.hrms.dto.response.employee.MyAllocationDTO;
import com.cagri.hrms.entity.core.LeaveDefinition;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.employee.Leave;
import com.cagri.hrms.enums.LeaveStatus;
import com.cagri.hrms.exception.BusinessException;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

    /** These statuses block overlap and (for pre-checks) count as usage. */
    private static final List<String> BLOCKING_STATUSES =
            List.of(LeaveStatus.PENDING.name(), LeaveStatus.APPROVED.name());

    /** For allocation/summary chips we usually show only APPROVED usage. */
    private static final List<String> APPROVED_ONLY =
            List.of(LeaveStatus.APPROVED.name());

    // ----------------------- small utilities -----------------------

    /** Inclusive day count (both start and end are counted). */
    private static int inclusiveDays(LocalDate start, LocalDate end) {
        return (int) (end.toEpochDay() - start.toEpochDay() + 1);
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
        if (end.isBefore(winStart) || start.isAfter(winEnd)) return 0;
        LocalDate s = start.isBefore(winStart) ? winStart : start;
        LocalDate e = end.isAfter(winEnd) ? winEnd : end;
        return inclusiveDays(s, e);
    }

    /** Prefer the boolean column on entity. */
    private boolean isAnnual(LeaveDefinition def) {
        return def != null && def.isAnnual();
    }

    // ----------------------- core flows -----------------------

    /**
     * Submit a new leave request.
     * - EMPLOYEE: can request for themselves, status = PENDING
     * - MANAGER: can assign to an employee in the same company, status = APPROVED
     */
    @Transactional
    @Override
    public void requestLeave(LeaveRequestDTO dto) {
        User currentUser = SecurityUtil.getCurrentUser();
        Employee targetEmployee;

        if (currentUser.hasRole("EMPLOYEE")) {
            targetEmployee = employeeRepository.findByUser(currentUser)
                    .orElseThrow(() -> new BusinessException("Employee not found"));
            dto.setEmployeeId(targetEmployee.getId());
        } else if (currentUser.hasRole("MANAGER")) {
            if (dto.getEmployeeId() == null) {
                throw new BusinessException("Manager must provide employeeId");
            }
            targetEmployee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new BusinessException("Employee not found"));

            if (targetEmployee.getUser().getId().equals(currentUser.getId())) {
                throw new BusinessException("Manager cannot request leave for themselves");
            }
            if (!targetEmployee.getCompany().getId().equals(currentUser.getCompany().getId())) {
                throw new BusinessException("You can only request leave for employees in your own company");
            }
        } else {
            throw new BusinessException("Unauthorized role");
        }

        if (dto.getLeaveDefinitionId() == null || dto.getLeaveDefinitionId() <= 0) {
            throw new BusinessException("Leave type (leaveDefinitionId) must be selected.");
        }
        LeaveDefinition leaveDefinition = leaveDefinitionRepository.findById(dto.getLeaveDefinitionId())
                .orElseThrow(() -> new BusinessException("Leave definition not found"));

        LocalDate start = dto.getStartDate();
        LocalDate end   = dto.getEndDate();
        if (start == null || end == null) throw new BusinessException("Start and end dates must be provided.");
        if (end.isBefore(start)) throw new BusinessException("End date cannot be earlier than start date.");

        // 1) Overlap guard
        boolean overlap = leaveRepository.existsOverlappingLeave(
                targetEmployee.getId(), start, end, BLOCKING_STATUSES);
        if (overlap) throw new BusinessException("Leave dates overlap with existing leave.");

        // 2) Annual quota guard (source of truth = Employee.annualLeave).
        if (isAnnual(leaveDefinition)) {
            Integer allowance = targetEmployee.getAnnualLeave();
            int totalDaysAllowed = allowance != null ? allowance : 0;
            if (totalDaysAllowed <= 0) {
                throw new BusinessException("No annual leave allocated by your manager.");
            }

            LocalDate ws = yearStart(start);
            LocalDate we = yearEnd(start);

            Integer used = leaveRepository.getUsedLeaveDaysInWindow(
                    targetEmployee.getId(), leaveDefinition.getId(), BLOCKING_STATUSES, ws, we);
            int already   = used != null ? used : 0;
            int remaining = Math.max(0, totalDaysAllowed - already);
            int requested = inclusiveDays(start, end);

            if (requested > remaining) {
                throw new BusinessException("Annual leave quota exceeded. Remaining days: " + remaining);
            }
        }

        // Persist
        Leave leave = leaveMapper.toEntity(dto);
        leave.setEmployee(targetEmployee);
        leave.setLeaveDefinition(leaveDefinition);
        leave.setRequestDate(LocalDate.now());
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
     * - On approval, re-validate overlap (excluding itself) and annual quota (Employee.annualLeave-based).
     */
    @Transactional
    @Override
    public void approveOrRejectLeave(LeaveApprovalDTO dto) {
        Leave leave = leaveRepository.findById(dto.getLeaveId())
                .orElseThrow(() -> new BusinessException("Leave not found"));

        if (leave.getStartDate() == null || leave.getEndDate() == null) {
            throw new BusinessException("Leave has invalid dates.");
        }
        if (leave.getEndDate().isBefore(leave.getStartDate())) {
            throw new BusinessException("Leave end date cannot be earlier than start date.");
        }

        // The manager who takes the decision
        User actingManager = SecurityUtil.getCurrentUser();
        leave.setManager(actingManager);

        if (dto.isApproved()) {
            // Overlap check against OTHER leaves (exclude this leave id)
            boolean overlap = leaveRepository.existsOverlappingLeaveExcept(
                    leave.getEmployee().getId(),
                    leave.getId(),
                    leave.getStartDate(),
                    leave.getEndDate(),
                    BLOCKING_STATUSES
            );
            if (overlap) {
                throw new BusinessException("Leave dates overlap with existing leave.");
            }

            // Annual quota check against Employee.annualLeave, excluding current PENDING slice from 'used'
            LeaveDefinition def = leave.getLeaveDefinition();
            if (def != null && isAnnual(def)) {
                LocalDate start = leave.getStartDate();
                LocalDate end   = leave.getEndDate();

                Integer allowance = leave.getEmployee().getAnnualLeave();
                int totalDaysAllowed = allowance != null ? allowance : 0;
                if (totalDaysAllowed <= 0) {
                    throw new BusinessException("No annual leave allocated for this employee.");
                }

                LocalDate ws = yearStart(start);
                LocalDate we = yearEnd(start);

                Integer used = leaveRepository.getUsedLeaveDaysInWindow(
                        leave.getEmployee().getId(), def.getId(), BLOCKING_STATUSES, ws, we);
                int usedTotal = used != null ? used : 0;

                int currentClipped = clippedInclusiveDays(start, end, ws, we);
                int usedExclCurrent = leave.getStatus() == LeaveStatus.PENDING
                        ? Math.max(0, usedTotal - currentClipped)
                        : usedTotal;

                if (usedExclCurrent + currentClipped > totalDaysAllowed) {
                    int remaining = Math.max(0, totalDaysAllowed - usedExclCurrent);
                    throw new BusinessException("Annual leave quota exceeded. Remaining days: " + remaining);
                }
            }

            leave.setStatus(LeaveStatus.APPROVED);
            leave.setDecisionDate(LocalDate.now());
        } else {
            leave.setStatus(LeaveStatus.REJECTED);
            leave.setDecisionDate(LocalDate.now());
        }

        leaveRepository.save(leave);

        // Notify the employee about the decision
        notificationService.sendLeaveDecisionNotification(leave.getEmployee(), dto.isApproved());
    }

    // ----------------------- queries -----------------------

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getAllLeaves() {
        return leaveRepository.findAll().stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getLeavesByEmployeeId(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("Employee not found"));
        return leaveRepository.findByEmployee(employee).stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getLeavesOfCurrentEmployee() {
        User currentUser = SecurityUtil.getCurrentUser();
        Employee employee = employeeRepository.findByUser(currentUser)
                .orElseThrow(() -> new BusinessException("Employee not found"));
        return leaveRepository.findByEmployee(employee).stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getApprovedLeaves() {
        return leaveRepository.findAllByStatus(LeaveStatus.APPROVED).stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getPendingLeaves() {
        return leaveRepository.findAllByStatus(LeaveStatus.PENDING).stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getRejectedLeaves() {
        return leaveRepository.findAllByStatus(LeaveStatus.REJECTED).stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getLeavesAssignedByManager() {
        User currentManager = authService.getCurrentUser();
        List<Leave> leaves = leaveRepository.findAllByCreatedBy_Id(currentManager.getId());
        return leaves.stream().map(leaveMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getLeavesWaitingForMyApproval() {
        User currentUser = userService.getCurrentUser();
        Long companyId = currentUser.getCompany().getId();
        List<Leave> pendingLeaves =
                leaveRepository.findByEmployee_Company_IdAndStatus(companyId, LeaveStatus.PENDING);
        return leaveMapper.toResponseDTOList(pendingLeaves);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getLeavesApprovedByManager() {
        User currentManager = authService.getCurrentUser();
        List<Leave> approvedLeaves =
                leaveRepository.findByStatusAndManager_Id(LeaveStatus.APPROVED, currentManager.getId());
        return approvedLeaves.stream().map(leaveMapper::toDto).toList();
    }

    // ----------------------- live pre-checks -----------------------

    @Override
    @Transactional(readOnly = true)
    public boolean checkOverlap(LeaveCheckDTO dto) {
        // Resolve employee id (manager assigns vs employee self)
        Long employeeId = dto.getEmployeeId() != null ? dto.getEmployeeId()
                : employeeRepository.findByUser(SecurityUtil.getCurrentUser())
                .orElseThrow(() -> new RuntimeException("Employee not found"))
                .getId();

        return leaveRepository.existsOverlappingLeave(
                employeeId,
                dto.getStartDate(),
                dto.getEndDate(),
                BLOCKING_STATUSES
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> checkAnnualQuota(LeaveCheckDTO dto) {
        var def = leaveDefinitionRepository.findById(dto.getLeaveDefinitionId())
                .orElseThrow(() -> new RuntimeException("Leave definition not found"));

        // Non-annual types always OK (e.g., Sick)
        if (!isAnnual(def)) return Map.of("ok", true);

        Long employeeId = dto.getEmployeeId() != null ? dto.getEmployeeId()
                : employeeRepository.findByUser(SecurityUtil.getCurrentUser())
                .orElseThrow(() -> new RuntimeException("Employee not found"))
                .getId();

        var emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Integer allowance = emp.getAnnualLeave();
        int totalDaysAllowed = allowance != null ? allowance : 0;
        if (totalDaysAllowed <= 0) {
            return Map.of("ok", false, "remainingDays", 0);
        }

        var start = dto.getStartDate();
        var end   = dto.getEndDate();
        var ws = yearStart(start);
        var we = yearEnd(start);

        Integer used = leaveRepository.getUsedLeaveDaysInWindow(
                employeeId, def.getId(), BLOCKING_STATUSES, ws, we);
        int already   = used != null ? used : 0;
        int remaining = Math.max(0, totalDaysAllowed - already);
        int requested = inclusiveDays(start, end);

        boolean ok = requested <= remaining;
        return Map.of("ok", ok, "remainingDays", remaining);
    }

    // ----------------------- allocations (for FE) -----------------------

    /**
     * Synthesized “allocation” summary for FE from Employee.annualLeave.
     * Returns at most one row (Annual Leave) with totalDays & usedDays.
     */
    @Override
    @Transactional(readOnly = true)
    public List<MyAllocationDTO> getMyAllocations() {
        var user = SecurityUtil.getCurrentUser();
        var emp = employeeRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("Employee not found"));

        // Find the Annual Leave definition (name-based; if you add findFirstByIsAnnualTrue(), prefer that)
        var annualDef = leaveDefinitionRepository.findByNameIgnoreCase("Annual Leave")
                .orElse(null);
        if (annualDef == null) {
            return List.of(); // no annual definition in the system
        }

        Integer allowance = emp.getAnnualLeave();
        int totalDaysAllowed = allowance != null ? allowance : 0;

        int year = LocalDate.now().getYear();
        LocalDate ws = LocalDate.of(year, 1, 1);
        LocalDate we = LocalDate.of(year, 12, 31);

        Integer used = leaveRepository.getUsedLeaveDaysInWindow(
                emp.getId(), annualDef.getId(), APPROVED_ONLY, ws, we);
        int usedDays = used != null ? used : 0;

        return List.of(new MyAllocationDTO(annualDef.getId(), totalDaysAllowed, usedDays));
    }
}
