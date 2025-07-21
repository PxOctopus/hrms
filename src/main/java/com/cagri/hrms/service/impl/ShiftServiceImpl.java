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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final CompanyRepository companyRepository;
    private final ShiftMapper shiftMapper;

    @Override
    public ShiftResponseDTO createShift(ShiftRequestDTO dto, User currentUser) {
        // Only allow the manager to create a shift for their own company
        if (!dto.getCompanyId().equals(currentUser.getCompany().getId())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "You can only create shifts for your own company.");
        }
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        // Use the mapper to convert dto to entity, then set the company manually
        Shift shift = shiftMapper.toEntity(dto);
        shift.setCompany(company);

        return shiftMapper.toDto(shiftRepository.save(shift));
    }

    @Override
    public ShiftResponseDTO updateShift(Long id, ShiftRequestDTO dto, User currentUser) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found"));
        // Only allow updating shift if it belongs to the manager's company
        if (!shift.getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "You can only update shifts of your own company.");
        }
        shift.setShiftName(dto.getShiftName());
        shift.setStartTime(dto.getStartTime());
        shift.setEndTime(dto.getEndTime());

        return shiftMapper.toDto(shiftRepository.save(shift));
    }

    @Override
    public void deleteShift(Long id, User currentUser) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found"));
        // Only allow deletion if the shift belongs to the manager's company
        if (!shift.getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "You can only delete shifts of your own company.");
        }
        shiftRepository.deleteById(id);
    }

    @Override
    public ShiftResponseDTO getShiftById(Long id, User currentUser) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found"));
        // Only allow access if the shift belongs to the user's company
        if (!shift.getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "You can only view shifts of your own company.");
        }
        return shiftMapper.toDto(shift);
    }

    @Override
    public List<ShiftResponseDTO> getShiftsByCompanyId(Long companyId, User currentUser) {
        // Only allow listing if the company belongs to the current user
        if (!companyId.equals(currentUser.getCompany().getId())) {
            throw new HrmsException(ErrorType.AUTHORIZATION_ERROR, "You can only view shifts for your own company.");
        }
        return shiftRepository.findAllByCompany_Id(companyId)
                .stream()
                .map(shiftMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShiftResponseDTO> getAllShifts(User currentUser) {
        // Only return shifts belonging to the current user's company
        return shiftRepository.findAllByCompany_Id(currentUser.getCompany().getId())
                .stream()
                .map(shiftMapper::toDto)
                .collect(Collectors.toList());
    }
}