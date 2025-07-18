package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.employee.BreakRequestDTO;
import com.cagri.hrms.dto.response.employee.BreakResponseDTO;

import java.util.List;

public interface BreakService {

    BreakResponseDTO createBreak(BreakRequestDTO requestDTO);

    BreakResponseDTO updateBreak(Long id, BreakRequestDTO requestDTO);

    void deleteBreak(Long id);

    List<BreakResponseDTO> getAllBreaks();

    List<BreakResponseDTO> getBreaksByEmployeeId(Long employeeId);
}
