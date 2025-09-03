package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.employee.LeaveApprovalDTO;
import com.cagri.hrms.dto.request.employee.LeaveCheckDTO;
import com.cagri.hrms.dto.request.employee.LeaveRequestDTO;
import com.cagri.hrms.dto.response.employee.LeaveResponseDTO;
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

    // These statuses both block overlap and count towards annual quota (use enum names for native SQL)
    private static final List<String> BLOCKING_STATUSES =
            List.of(LeaveStatus.PENDING.name(), LeaveStatus.APPROVED.name());

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

    /** Prefer a real boolean property (isAnnual) if present; otherwise fallback to name check. */
    private boolean isAnnual(LeaveDefinition def) {
        try {
            var m = def.getClass().getMethod("getIsAnnual");
            Object v = m.invoke(def);
            if (v instanceof Boolean b && b != null) return b;
        } catch (Exception ignored) {}
        String name = def.getName();
        return name != null && name.toLowerCase().contains("annual");
    }

    /**
     * Enforce annual allowance per calendar year window. If a request spans multiple years,
     * validate each year separately by clipping to that year's [Jan 1, Dec 31].
     */
    private void enforceAnnualQuota(Long employeeId, LeaveDefinition def, LocalDate start, LocalDate end) {
        if (!isAnnual(def)) return;

        Integer max = def.getMaxDays();
        int allowance = max != null ? max : 0;
        if (allowance <= 0) {
            throw new BusinessException("Annual leave allowance is not configured.");
        }

        for (int year = start.getYear(); year <= end.getYear(); year++) {
            LocalDate winStart = LocalDate.of(year, 1, 1);
            LocalDate winEnd   = LocalDate.of(year, 12, 31);

            LocalDate a = start.isAfter(winStart) ? start : winStart;
            LocalDate b = end.isBefore(winEnd)   ? end   : winEnd;
            if (a.isAfter(b)) continue; // no overlap with this year

            int requestedDaysInYear = inclusiveDays(a, b);

            Integer used = leaveRepository.getUsedLeaveDaysInWindow(
                    employeeId, def.getId(), BLOCKING_STATUSES, a, b);
            int usedDays = used != null ? used : 0;

            if (usedDays + requestedDaysInYear > allowance) {
                int remaining = Math.max(0, allowance - usedDays);
                throw new BusinessException("Annual leave quota exceeded for " + year +
                        ". Remaining days: " + remaining);
            }
        }
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

        // 2) Annual quota guard (applies to both EMPLOYEE requests and MANAGER assignments)
        enforceAnnualQuota(targetEmployee.getId(), leaveDefinition, start, end);

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
     * - On approval, re-validate overlap (excluding itself) and annual quota.
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

            // Annual quota check (exclude current PENDING slice from 'used' by subtracting its clipped days)
            LeaveDefinition def = leave.getLeaveDefinition();
            if (def != null && isAnnual(def)) {
                LocalDate start = leave.getStartDate();
                LocalDate end   = leave.getEndDate();

                LocalDate ws = yearStart(start);
                LocalDate we = yearEnd(start);

                Integer used = leaveRepository.getUsedLeaveDaysInWindow(
                        leave.getEmployee().getId(), def.getId(), BLOCKING_STATUSES, ws, we
                );
                int usedTotal = used != null ? used : 0;

                // Current leave (PENDING) is likely included in usedTotal; subtract its clipped portion.
                int currentClipped = clippedInclusiveDays(start, end, ws, we);
                int usedExcludingCurrent = leave.getStatus() == LeaveStatus.PENDING
                        ? Math.max(0, usedTotal - currentClipped)
                        : usedTotal;

                int allowance = def.getMaxDays() != null ? def.getMaxDays() : 0;
                if (usedExcludingCurrent + currentClipped > allowance) {
                    int remaining = Math.max(0, allowance - usedExcludingCurrent);
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
    public List<LeaveResponseDTO> getAllLeaves() {
        return leaveRepository.findAll().stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    @Override
    public List<LeaveResponseDTO> getLeavesByEmployeeId(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("Employee not found"));
        return leaveRepository.findByEmployee(employee).stream()
                .map(leaveMapper::toDto)
                .toList();
    }

    @Override
    public List<LeaveResponseDTO> getLeavesOfCurrentEmployee() {
        User currentUser = SecurityUtil.getCurrentUser();
        Employee employee = employeeRepository.findByUser(currentUser)
                .orElseThrow(() -> new BusinessException("Employee not found"));
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


    @Override
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
    public Map<String, Object> checkAnnualQuota(LeaveCheckDTO dto) {
        var def = leaveDefinitionRepository.findById(dto.getLeaveDefinitionId())
                .orElseThrow(() -> new RuntimeException("Leave definition not found"));

        // Non-annual types always OK
        if (!isAnnual(def)) return Map.of("ok", true);

        Long employeeId = dto.getEmployeeId() != null ? dto.getEmployeeId()
                : employeeRepository.findByUser(SecurityUtil.getCurrentUser())
                .orElseThrow(() -> new RuntimeException("Employee not found"))
                .getId();

        var start = dto.getStartDate();
        var end   = dto.getEndDate();
        var ws = yearStart(start);
        var we = yearEnd(start);

        Integer used = leaveRepository.getUsedLeaveDaysInWindow(
                employeeId, def.getId(), BLOCKING_STATUSES, ws, we
        );
        int already   = used != null ? used : 0;
        int requested = inclusiveDays(start, end);
        int allowance = def.getMaxDays() != null ? def.getMaxDays() : 0;

        boolean ok = already + requested <= allowance;
        int remaining = Math.max(0, allowance - already);

        return ok
                ? Map.of("ok", true,  "remainingDays", remaining)
                : Map.of("ok", false, "remainingDays", remaining);
    }
}
