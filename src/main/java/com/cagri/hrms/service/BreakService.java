package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.employee.BreakRequestDTO;
import com.cagri.hrms.dto.response.employee.BreakResponseDTO;
import com.cagri.hrms.entity.core.User;

import java.util.List;

public interface BreakService {

    BreakResponseDTO createBreak(BreakRequestDTO requestDTO, User currentUser);

    BreakResponseDTO updateBreak(Long id, BreakRequestDTO requestDTO, User currentUser);

    void deleteBreak(Long id, User currentUser);

    List<BreakResponseDTO> getAllBreaks(User currentUser);

    List<BreakResponseDTO> getBreaksByEmployeeId(Long employeeId, User currentUser);
}
