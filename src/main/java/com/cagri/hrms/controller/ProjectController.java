//package com.cagri.hrms.controller;
//
//import com.cagri.hrms.dto.request.expense.ProjectCreateDTO;
//import com.cagri.hrms.dto.response.expense.ProjectResponseDTO;
//import com.cagri.hrms.entity.core.Company;
//import com.cagri.hrms.service.CompanyService;
//import com.cagri.hrms.service.ProjectService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.web.PageableDefault;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/projects")
//@RequiredArgsConstructor
//public class ProjectController {
//
//    private final ProjectService projectService;
//    private final CompanyService companyService;
//
//    // Resolve current company (role-aware: works for MANAGER or EMPLOYEE).
//    // Throws if the current user has no company scope.
//    private Company currentCompany() {
//        return companyService.getCurrentCompanyOrThrow();
//    }
//
//    // MANAGER: create a new project under the current company
//    @PreAuthorize("hasRole('MANAGER')")
//    @PostMapping
//    public ProjectResponseDTO create(@Valid @RequestBody ProjectCreateDTO dto) {
//        return projectService.createProject(dto, currentCompany());
//    }
//
//    // MANAGER/EMPLOYEE: list projects for the current company (typically active ones)
//    @PreAuthorize("hasAnyRole('MANAGER','EMPLOYEE')")
//    @GetMapping
//    public Page<ProjectResponseDTO> list(@PageableDefault(size = 20) Pageable pageable) {
//        return projectService.listActive(currentCompany(), pageable);
//    }
//
//    // MANAGER/EMPLOYEE: get a single project by id within the current company
//    @PreAuthorize("hasAnyRole('MANAGER','EMPLOYEE')")
//    @GetMapping("/{id}")
//    public ProjectResponseDTO get(@PathVariable Long id) {
//        return projectService.getById(id, currentCompany());
//    }
//}
