//package com.cagri.hrms.controller;
//
//import com.cagri.hrms.dto.response.expense.ProjectAssignmentRespondDTO;
//import com.cagri.hrms.dto.response.expense.ProjectAssignmentResponseDTO;
//import com.cagri.hrms.entity.employee.Employee;
//import com.cagri.hrms.service.CompanyService;
//import com.cagri.hrms.service.EmployeeService;
//import com.cagri.hrms.service.ProjectAssignmentService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/project-assignments")
//@RequiredArgsConstructor
//public class ProjectAssignmentController {
//
//    private final ProjectAssignmentService service;
//    private final CompanyService companyService;
//    private final EmployeeService employeeService;
//
//    private Employee currentEmployee() {
//        Long userId = companyService.getCurrentUserId();
//        return employeeService.findByUserId(userId)
//                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Employee not found for current user"));
//    }
//
//    private Long currentUserId() {
//        return companyService.getCurrentUserId();
//    }
//
//    @PreAuthorize("hasRole('MANAGER')")
//    @PostMapping("/assign")
//    public ProjectAssignmentResponseDTO assign(@RequestParam Long projectId, @RequestParam Long employeeId) {
//        return service.assignEmployee(projectId, employeeId, currentUserId());
//    }
//
//    @PreAuthorize("hasRole('EMPLOYEE')")
//    @PostMapping("/{id}/respond")
//    public ProjectAssignmentResponseDTO respond(@PathVariable Long id, @Valid @RequestBody ProjectAssignmentRespondDTO dto) {
//        return service.respondAssignment(id, dto.isAccepted(), currentUserId());
//    }
//
//    @PreAuthorize("hasRole('EMPLOYEE')")
//    @GetMapping("/my")
//    public List<ProjectAssignmentResponseDTO> myAssignments() {
//        return service.listForEmployee(currentEmployee());
//    }
//}
