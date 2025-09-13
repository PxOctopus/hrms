package com.cagri.hrms.controller;

import com.cagri.hrms.dto.response.expense.ProjectAssignmentRespondDTO;
import com.cagri.hrms.dto.response.expense.ProjectAssignmentResponseDTO;
import com.cagri.hrms.entity.employee.Employee;
import com.cagri.hrms.service.ProjectAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project-assignments")
@RequiredArgsConstructor
public class ProjectAssignmentController {

    private final ProjectAssignmentService service;

    private Employee currentEmployee() { /* ... */ return null; }
    private Long currentUserId() { /* ... */ return null; }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/assign")
    public ProjectAssignmentResponseDTO assign(@RequestParam Long projectId, @RequestParam Long employeeId) {
        return service.assignEmployee(projectId, employeeId, currentUserId());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{id}/respond")
    public ProjectAssignmentResponseDTO respond(@PathVariable Long id, @Valid @RequestBody ProjectAssignmentRespondDTO dto) {
        return service.respondAssignment(id, dto.isAccepted(), currentUserId());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/my")
    public List<ProjectAssignmentResponseDTO> myAssignments() {
        return service.listForEmployee(currentEmployee());
    }
}
