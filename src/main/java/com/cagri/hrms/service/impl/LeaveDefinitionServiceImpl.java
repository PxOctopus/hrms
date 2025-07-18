package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.general.LeaveDefinitionRequestDTO;
import com.cagri.hrms.dto.response.general.LeaveDefinitionResponseDTO;
import com.cagri.hrms.entity.core.LeaveDefinition;
import com.cagri.hrms.mapper.LeaveDefinitionMapper;
import com.cagri.hrms.repository.LeaveDefinitionRepository;
import com.cagri.hrms.service.LeaveDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveDefinitionServiceImpl implements LeaveDefinitionService {

    private final LeaveDefinitionRepository leaveDefinitionRepository;
    private final LeaveDefinitionMapper leaveDefinitionMapper;

    @Override
    public void addLeaveDefinition(LeaveDefinitionRequestDTO dto) {
        LeaveDefinition definition = leaveDefinitionMapper.toEntity(dto);
        leaveDefinitionRepository.save(definition);
    }

    @Override
    public List<LeaveDefinitionResponseDTO> getAllLeaveDefinitions() {
        return leaveDefinitionRepository.findAll()
                .stream()
                .map(leaveDefinitionMapper::toDto)
                .toList();
    }
}
