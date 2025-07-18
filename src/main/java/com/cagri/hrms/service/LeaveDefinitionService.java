package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.general.LeaveDefinitionRequestDTO;
import com.cagri.hrms.dto.response.general.LeaveDefinitionResponseDTO;

import java.util.List;

public interface LeaveDefinitionService {
    void addLeaveDefinition(LeaveDefinitionRequestDTO dto);
    List<LeaveDefinitionResponseDTO> getAllLeaveDefinitions();
}
