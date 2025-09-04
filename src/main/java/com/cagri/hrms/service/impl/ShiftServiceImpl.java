package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.employee.ShiftRequestDTO;
import com.cagri.hrms.dto.response.employee.ShiftResponseDTO;
import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.entity.employee.Shift;
import com.cagri.hrms.exception.ErrorType;
import com.cagri.hrms.exception.HrmsException;
import com.cagri.hrms.exception.ResourceNotFoundException;
import com.cagri.hrms.mapper.ShiftMapper;
import com.cagri.hrms.repository.CompanyRepository;
import com.cagri.hrms.repository.ShiftRepository;
import com.cagri.hrms.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ShiftService implementation:
 *  - Company derives from auth (manager's company)
 *  - Admin/global ops are NOT supported here
 *  - Overnight (end <= start) is allowed — no validation here; handled in assignment service when needed
 */
@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final CompanyRepository companyRepository;
    private final ShiftMapper shiftMapper;

    private static final String ROLE_MANAGER = "MANAGER";

    private boolean isManager(User user) {
        return user != null && user.getRole() != null && ROLE_MANAGER.equalsIgnoreCase(user.getRole().getName());
    }

    @Override
    @Transactional
    public ShiftResponseDTO createShift(ShiftRequestDTO dto, User currentUser) {
        // Role guard: managers only
        if (!isManager(currentUser)) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Only managers can create shifts.");
        }

        // Company derives from auth (ignore DTO.companyId)
        Long companyId = currentUser.getCompany().getId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Shift shift = shiftMapper.toEntity(dto);
        shift.setCompany(company);

        // (Optional) add additional domain checks if needed

        return shiftMapper.toDto(shiftRepository.save(shift));
    }

    @Override
    @Transactional
    public ShiftResponseDTO updateShift(Long id, ShiftRequestDTO dto, User currentUser) {
        if (!isManager(currentUser)) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Only managers can update shifts.");
        }

        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found"));

        // Company guard
        if (!shift.getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "You can only update shifts of your own company.");
        }

        shift.setShiftName(dto.getShiftName());
        shift.setStartTime(dto.getStartTime());
        shift.setEndTime(dto.getEndTime());

        return shiftMapper.toDto(shiftRepository.save(shift));
    }

    @Override
    @Transactional
    public void deleteShift(Long id, User currentUser) {
        if (!isManager(currentUser)) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "Only managers can delete shifts.");
        }

        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found"));

        if (!shift.getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "You can only delete shifts of your own company.");
        }

        shiftRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftResponseDTO getShiftById(Long id, User currentUser) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found"));

        // Allow both MANAGER and EMPLOYEE, but only within same company
        if (!shift.getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "You can only view shifts of your own company.");
        }
        return shiftMapper.toDto(shift);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftResponseDTO> getMyCompanyShifts(User currentUser) {
        // Managers and employees can list shifts, but scope is always their company
        Long companyId = currentUser.getCompany().getId();
        return shiftRepository.findAllByCompany_IdOrderByStartTimeAsc(companyId)
                .stream()
                .map(shiftMapper::toDto)
                .collect(Collectors.toList());
    }
}
