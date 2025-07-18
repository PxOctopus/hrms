package com.cagri.hrms.controller;

import com.cagri.hrms.dto.request.general.LeaveDefinitionRequestDTO;
import com.cagri.hrms.dto.response.general.LeaveDefinitionResponseDTO;
import com.cagri.hrms.service.LeaveDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-definitions")
@RequiredArgsConstructor
public class LeaveDefinitionController {

    private final LeaveDefinitionService leaveDefinitionService;

    // Only MANAGER can create leave definitions
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> addLeaveDefinition(@RequestBody LeaveDefinitionRequestDTO dto) {
        leaveDefinitionService.addLeaveDefinition(dto);
        return ResponseEntity.ok().build();
    }

    // Only MANAGER can view all leave definitions
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<LeaveDefinitionResponseDTO>> getAllLeaveDefinitions() {
        return ResponseEntity.ok(leaveDefinitionService.getAllLeaveDefinitions());
    }
}
