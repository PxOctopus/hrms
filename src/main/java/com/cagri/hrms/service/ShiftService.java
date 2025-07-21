package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.employee.ShiftRequestDTO;
import com.cagri.hrms.dto.response.employee.ShiftResponseDTO;
import com.cagri.hrms.entity.core.User;

import java.util.List;

public interface ShiftService {
    ShiftResponseDTO createShift(ShiftRequestDTO dto, User currentUser);
    ShiftResponseDTO updateShift(Long id, ShiftRequestDTO dto, User currentUser);
    void deleteShift(Long id, User currentUser);
    ShiftResponseDTO getShiftById(Long id, User currentUser);
    List<ShiftResponseDTO> getShiftsByCompanyId(Long companyId, User currentUser);
    List<ShiftResponseDTO> getAllShifts(User currentUser);
}
