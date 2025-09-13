package com.cagri.hrms.controller;

import com.cagri.hrms.dto.request.expense.ProjectCreateDTO;
import com.cagri.hrms.dto.response.expense.ProjectResponseDTO;
import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService service;

    // TODO: Resolve company from auth context in a @ControllerAdvice or argument resolver
    private Company currentCompany() { /* ... */ return null; }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public ProjectResponseDTO create(@Valid @RequestBody ProjectCreateDTO dto) {
        return service.createProject(dto, currentCompany());
    }

    @PreAuthorize("hasAnyRole('MANAGER','EMPLOYEE')")
    @GetMapping
    public Page<ProjectResponseDTO> list(@PageableDefault(size = 20) Pageable pageable) {
        return service.listActive(currentCompany(), pageable);
    }

    @PreAuthorize("hasAnyRole('MANAGER','EMPLOYEE')")
    @GetMapping("/{id}")
    public ProjectResponseDTO get(@PathVariable Long id) {
        return service.getById(id, currentCompany());
    }
}
