package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.employee.EmployeeShiftRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeShiftResponseDTO;
import com.cagri.hrms.dto.response.employee.EmployeeShiftWeekItemDTO;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.entity.employee.EmployeeShift;
import com.cagri.hrms.entity.employee.Shift;
import com.cagri.hrms.enums.LeaveStatus;
import com.cagri.hrms.exception.ErrorType;
import com.cagri.hrms.exception.HrmsException;
import com.cagri.hrms.mapper.EmployeeShiftMapper;
import com.cagri.hrms.repository.EmployeeRepository;
import com.cagri.hrms.repository.EmployeeShiftRepository;
import com.cagri.hrms.repository.LeaveRepository;
import com.cagri.hrms.repository.ShiftRepository;
import com.cagri.hrms.service.EmployeeShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeShiftServiceImpl implements EmployeeShiftService {

    private final EmployeeShiftRepository employeeShiftRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final LeaveRepository leaveRepository;
    private final EmployeeShiftMapper employeeShiftMapper;

    private static final String ROLE_MANAGER = "MANAGER";

    private boolean isManager(User user) {
        return user != null && user.getRole() != null && ROLE_MANAGER.equalsIgnoreCase(user.getRole().getName());
    }

    // ---------- CREATE ----------

    /**
     * Assign a single-day shift to an employee.
     * Overnight is supported: if shift.endTime <= shift.startTime, we still create ONE record for D,
     * but we also check APPROVED leave on D+1 to prevent overlap with the next day.
     */
    @Override
    @Transactional
    public EmployeeShiftResponseDTO assignEmployeeShift(EmployeeShiftRequestDTO dto, User currentUser) {
        // Role guard: managers only
        if (!isManager(currentUser)) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Only managers can assign shifts.");
        }

        final Long companyId = currentUser.getCompany().getId();

        // Company guard — quick existence checks before loading entities
        if (!employeeRepository.existsByIdAndCompany_Id(dto.getEmployeeId(), companyId)) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Employee does not belong to your company.");
        }
        if (!shiftRepository.existsByIdAndCompany_Id(dto.getShiftId(), companyId)) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Shift does not belong to your company.");
        }

        // Load aggregates (with user/company prefetched for mapper/readability)
        Employee employee = employeeRepository.findWithUserAndCompanyById(dto.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        Shift shift = shiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new EntityNotFoundException("Shift not found"));

        LocalDate date = dto.getShiftDate();

        // Leave guard for D
        if (leaveRepository.existsApprovedOn(employee.getId(), date, LeaveStatus.APPROVED)) {
            throw new HrmsException(ErrorType.BUSINESS_ERROR, "Employee is on approved leave for " + date);
        }

        // Overnight detection: end <= start means it spills to next calendar day
        boolean overnight = !shift.getEndTime().isAfter(shift.getStartTime());
        if (overnight) {
            LocalDate nextDay = date.plusDays(1);
            if (leaveRepository.existsApprovedOn(employee.getId(), nextDay, LeaveStatus.APPROVED)) {
                throw new HrmsException(ErrorType.BUSINESS_ERROR, "Overnight shift overlaps approved leave on " + nextDay);
            }
        }

        // Duplicate guard (one assignment per employee per date)
        employeeShiftRepository.findByEmployee_IdAndShiftDate(employee.getId(), date).ifPresent(es -> {
            throw new HrmsException(ErrorType.BUSINESS_ERROR, "Employee already has a shift on " + date);
        });

        EmployeeShift entity = employeeShiftMapper.toEntity(dto, employee, shift);
        entity.setShiftDate(date);
        entity.setActive(dto.getActive() == null || dto.getActive());

        return employeeShiftMapper.toDto(employeeShiftRepository.save(entity));
    }

    // ---------- UPDATE ----------

    /**
     * Update an existing single-day assignment. All guards from create() apply.
     * If employee or date changes, duplicate guard re-checked.
     * Overnight leave guard is recalculated against the (possibly) new date/shift.
     */
    @Override
    @Transactional
    public EmployeeShiftResponseDTO updateEmployeeShift(Long id, EmployeeShiftRequestDTO dto, User currentUser) {
        if (!isManager(currentUser)) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Only managers can update shifts.");
        }

        EmployeeShift employeeShift = employeeShiftRepository.findById(id)
                .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "EmployeeShift not found"));

        Long companyId = currentUser.getCompany().getId();
        Long assignmentCompanyId = employeeShift.getEmployee().getCompany().getId();
        if (!companyId.equals(assignmentCompanyId)) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Cannot update another company's shift assignment.");
        }

        // Possibly change employee (company guard)
        if (!employeeShift.getEmployee().getId().equals(dto.getEmployeeId())) {
            if (!employeeRepository.existsByIdAndCompany_Id(dto.getEmployeeId(), companyId)) {
                throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Employee does not belong to your company.");
            }
            Employee newEmployee = employeeRepository.findWithUserAndCompanyById(dto.getEmployeeId())
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
            employeeShift.setEmployee(newEmployee);
        }

        // Possibly change shift (company guard)
        if (!employeeShift.getShift().getId().equals(dto.getShiftId())) {
            if (!shiftRepository.existsByIdAndCompany_Id(dto.getShiftId(), companyId)) {
                throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Shift does not belong to your company.");
            }
            Shift newShift = shiftRepository.findById(dto.getShiftId())
                    .orElseThrow(() -> new EntityNotFoundException("Shift not found"));
            employeeShift.setShift(newShift);
        }

        // Check duplicate if date (or employee) changed
        LocalDate newDate = dto.getShiftDate();
        if (!employeeShift.getShiftDate().equals(newDate) ||
                !employeeShift.getEmployee().getId().equals(dto.getEmployeeId())) {
            employeeShiftRepository.findByEmployee_IdAndShiftDate(dto.getEmployeeId(), newDate).ifPresent(es -> {
                if (!es.getId().equals(id)) {
                    throw new HrmsException(ErrorType.BUSINESS_ERROR, "Employee already has a shift on " + newDate);
                }
            });
        }

        // Leave guard (for D and D+1 if overnight)
        Shift effectiveShift = employeeShift.getShift(); // might be changed above
        boolean overnight = !effectiveShift.getEndTime().isAfter(effectiveShift.getStartTime());

        if (leaveRepository.existsApprovedOn(employeeShift.getEmployee().getId(), newDate, LeaveStatus.APPROVED)) {
            throw new HrmsException(ErrorType.BUSINESS_ERROR, "Employee is on approved leave for " + newDate);
        }
        if (overnight) {
            LocalDate nextDay = newDate.plusDays(1);
            if (leaveRepository.existsApprovedOn(employeeShift.getEmployee().getId(), nextDay, LeaveStatus.APPROVED)) {
                throw new HrmsException(ErrorType.BUSINESS_ERROR, "Overnight shift overlaps approved leave on " + nextDay);
            }
        }

        employeeShift.setShiftDate(newDate);
        employeeShift.setActive(dto.getActive() == null ? employeeShift.isActive() : dto.getActive());

        return employeeShiftMapper.toDto(employeeShiftRepository.save(employeeShift));
    }

    // ---------- DELETE (soft) ----------

    @Override
    @Transactional
    public void deleteEmployeeShift(Long id, User currentUser) {
        if (!isManager(currentUser)) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Only managers can delete shifts.");
        }
        EmployeeShift employeeShift = employeeShiftRepository.findById(id)
                .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "EmployeeShift not found"));

        if (!employeeShift.getEmployee().getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Cannot delete another company's shift assignment.");
        }

        // Soft delete (keep history)
        employeeShift.setActive(false);
        employeeShiftRepository.save(employeeShift);
    }

    // ---------- READ ----------

    @Override
    @Transactional(readOnly = true)
    public EmployeeShiftResponseDTO getEmployeeShiftById(Long id, User currentUser) {
        EmployeeShift employeeShift = employeeShiftRepository.findById(id)
                .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "EmployeeShift not found"));

        boolean managerSameCompany = isManager(currentUser)
                && employeeShift.getEmployee().getCompany().getId().equals(currentUser.getCompany().getId());
        boolean selfAccess = employeeShift.getEmployee().getUser().getId().equals(currentUser.getId());

        if (!managerSameCompany && !selfAccess) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Not authorized to view this shift assignment.");
        }
        return employeeShiftMapper.toDto(employeeShift);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeShiftResponseDTO> getEmployeeShiftsByEmployeeId(Long employeeId, User currentUser) {
        boolean managerSameCompany = isManager(currentUser)
                && employeeRepository.existsByIdAndCompany_Id(employeeId, currentUser.getCompany().getId());
        boolean selfAccess = employeeRepository.findByUserId(currentUser.getId())
                .map(emp -> emp.getId().equals(employeeId))
                .orElse(false);

        if (!managerSameCompany && !selfAccess) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Not authorized to view these shift assignments.");
        }

        return employeeShiftRepository.findAllByEmployee_Id(employeeId)
                .stream()
                .map(employeeShiftMapper::toDto)
                .collect(Collectors.toList());
    }

    // ---------- RANGE (weekly/grid) ----------

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeShiftWeekItemDTO> getEmployeeShiftsInRange(Long employeeId, LocalDate from, LocalDate to, User currentUser) {
        // Company guard
        if (!employeeRepository.existsByIdAndCompany_Id(employeeId, currentUser.getCompany().getId())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Employee does not belong to your company.");
        }
        // Use company-scoped fetch-join query to reduce leakage risk and avoid N+1
        return employeeShiftRepository.findActiveByEmployeeAndRangeInCompany(employeeId, currentUser.getCompany().getId(), from, to)
                .stream()
                .map(es -> new EmployeeShiftWeekItemDTO(
                        es.getId(),
                        es.getShift().getId(),
                        es.getShift().getShiftName(),
                        es.getShiftDate(),
                        es.getShift().getStartTime(),
                        es.getShift().getEndTime()
                ))
                .collect(Collectors.toList());
    }
}
