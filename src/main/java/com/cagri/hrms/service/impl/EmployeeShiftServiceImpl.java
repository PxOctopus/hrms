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

    @Override
    @Transactional
    public EmployeeShiftResponseDTO assignEmployeeShift(EmployeeShiftRequestDTO dto, User currentUser) {
        if (!isManager(currentUser)) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Only managers can assign shifts.");
        }

        final Long companyId = currentUser.getCompany().getId();

        // Company guards for employee and shift
        if (!employeeRepository.existsByIdAndCompany_Id(dto.getEmployeeId(), companyId)) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Employee does not belong to your company.");
        }
        if (!shiftRepository.existsByIdAndCompany_Id(dto.getShiftId(), companyId)) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Shift does not belong to your company.");
        }

        Employee employee = employeeRepository.findByIdWithUserAndCompany(dto.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        Shift shift = shiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new EntityNotFoundException("Shift not found"));

        // Eligibility
        if (!employee.isActive()
                || Boolean.TRUE.equals(employee.getIsPendingApprovalByManager())
                || employee.getUser() == null
                || !employee.getUser().isEnabled()
                || !Boolean.TRUE.equals(employee.getUser().getEmailVerified())) {
            throw new HrmsException(ErrorType.BUSINESS_ERROR, "Employee is not eligible for assignment.");
        }

        LocalDate date = dto.getShiftDate();

        // Leave guard for D
        if (leaveRepository.existsApprovedOn(employee.getId(), date, LeaveStatus.APPROVED)) {
            throw new HrmsException(ErrorType.BUSINESS_ERROR, "Employee is on approved leave for " + date);
        }

        // Overnight guard for D+1 if needed
        boolean overnight = !shift.getEndTime().isAfter(shift.getStartTime());
        if (overnight) {
            LocalDate nextDay = date.plusDays(1);
            if (leaveRepository.existsApprovedOn(employee.getId(), nextDay, LeaveStatus.APPROVED)) {
                throw new HrmsException(ErrorType.BUSINESS_ERROR, "Overnight shift overlaps approved leave on " + nextDay);
            }
        }

        // Duplicate guard with soft delete:
        // 1) If an ACTIVE row exists -> error.
        // 2) Else if an INACTIVE row exists for the same (employee, date) -> REACTIVATE it and update shift.
        // 3) Else create new.
        var existingAny = employeeShiftRepository.findByEmployee_IdAndShiftDate(employee.getId(), date);

        if (existingAny.isPresent()) {
            EmployeeShift existing = existingAny.get();
            if (existing.isActive()) {
                throw new HrmsException(ErrorType.BUSINESS_ERROR, "Employee already has a shift on " + date);
            }
            // Reactivate the existing soft-deleted row instead of inserting a new one
            existing.setShift(shift);
            existing.setActive(true);
            // If you want to reset breaks when reactivating, clear the collection here.
            // existing.getBreaks().clear();
            return employeeShiftMapper.toDto(employeeShiftRepository.save(existing));
        }

        // No row exists at all -> create new
        EmployeeShift entity = employeeShiftMapper.toEntity(dto, employee, shift);
        entity.setShiftDate(date);
        entity.setActive(true);
        return employeeShiftMapper.toDto(employeeShiftRepository.save(entity));
    }

    // ---------- UPDATE ----------

    @Override
    @Transactional
    public EmployeeShiftResponseDTO updateEmployeeShift(Long id, EmployeeShiftRequestDTO dto, User currentUser) {
        if (!isManager(currentUser)) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Only managers can update shifts.");
        }

        EmployeeShift employeeShift = employeeShiftRepository.findById(id)
                .orElseThrow(() -> new HrmsException(ErrorType.RESOURCE_NOT_FOUND, "EmployeeShift not found"));

        Long companyId = currentUser.getCompany().getId();
        if (!companyId.equals(employeeShift.getEmployee().getCompany().getId())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Cannot update another company's shift assignment.");
        }

        // Change employee if needed (with company + eligibility guard)
        if (!employeeShift.getEmployee().getId().equals(dto.getEmployeeId())) {
            if (!employeeRepository.existsByIdAndCompany_Id(dto.getEmployeeId(), companyId)) {
                throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Employee does not belong to your company.");
            }
            Employee newEmployee = employeeRepository.findByIdWithUserAndCompany(dto.getEmployeeId())
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

            if (!newEmployee.isActive()
                    || Boolean.TRUE.equals(newEmployee.getIsPendingApprovalByManager())
                    || newEmployee.getUser() == null
                    || !newEmployee.getUser().isEnabled()
                    || !Boolean.TRUE.equals(newEmployee.getUser().getEmailVerified())) {
                throw new HrmsException(ErrorType.BUSINESS_ERROR, "Employee is not eligible for assignment.");
            }
            employeeShift.setEmployee(newEmployee);
        }

        // Change shift if needed (company guard)
        if (!employeeShift.getShift().getId().equals(dto.getShiftId())) {
            if (!shiftRepository.existsByIdAndCompany_Id(dto.getShiftId(), companyId)) {
                throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Shift does not belong to your company.");
            }
            Shift newShift = shiftRepository.findById(dto.getShiftId())
                    .orElseThrow(() -> new EntityNotFoundException("Shift not found"));
            employeeShift.setShift(newShift);
        }

        LocalDate newDate = dto.getShiftDate();

        // Duplicate guard on change of (employee or date)
        if (!employeeShift.getShiftDate().equals(newDate)
                || !employeeShift.getEmployee().getId().equals(dto.getEmployeeId())) {
            employeeShiftRepository.findByEmployee_IdAndShiftDate(dto.getEmployeeId(), newDate).ifPresent(es -> {
                // Block only if the found row is ACTIVE and not the same id
                if (es.isActive() && !es.getId().equals(id)) {
                    throw new HrmsException(ErrorType.BUSINESS_ERROR, "Employee already has a shift on " + newDate);
                }
            });
        }

        // Leave guard for D (and D+1 if overnight)
        Shift effectiveShift = employeeShift.getShift();
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

        // Allow toggling active from update only if you want admin-like behavior.
        // Usually we keep active=true here and handle deletions via delete().
        if (dto.getActive() != null) {
            employeeShift.setActive(dto.getActive());
        }

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

        // Use repository.delete to trigger @SQLDelete (sets active=false)
        employeeShiftRepository.delete(employeeShift);

        // If you also want to soft-delete related breaks, consider adding a soft flag to Break
        // and handling it via application logic. Cascade will not fire @SQLDelete on children automatically.
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

        // If you want only active here, switch to a query that filters es.active = true.
        return employeeShiftRepository.findAllByEmployee_Id(employeeId)
                .stream()
                .filter(EmployeeShift::isActive) // keep UI clean by default
                .map(employeeShiftMapper::toDto)
                .collect(Collectors.toList());
    }

    // ---------- RANGE (weekly/grid) ----------

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeShiftWeekItemDTO> getEmployeeShiftsInRange(Long employeeId, LocalDate from, LocalDate to, User currentUser) {
        if (!employeeRepository.existsByIdAndCompany_Id(employeeId, currentUser.getCompany().getId())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Employee does not belong to your company.");
        }
        return employeeShiftRepository
                .findActiveByEmployeeAndRangeInCompany(employeeId, currentUser.getCompany().getId(), from, to)
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
