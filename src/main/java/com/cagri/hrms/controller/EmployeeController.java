package com.cagri.hrms.controller;

import com.cagri.hrms.dto.request.employee.EmployeeCreateRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeResponseDTO;
import com.cagri.hrms.entity.User;
import com.cagri.hrms.mapper.EmployeeMapper;
import com.cagri.hrms.service.EmployeeService;
import com.cagri.hrms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final UserService userService;

    @PreAuthorize("hasRole('COMPANY_MANAGER')")
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @RequestBody @Valid EmployeeCreateRequestDTO createDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User authenticatedUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(employeeService.createEmployee(createDTO, authenticatedUser));
    }

    @PreAuthorize("hasRole('COMPANY_MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PreAuthorize("hasRole('COMPANY_MANAGER')")
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @PreAuthorize("hasRole('COMPANY_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable Long id,
            @RequestBody @Valid EmployeeCreateRequestDTO updateDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User authenticatedUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(employeeService.updateEmployee(id, updateDTO, authenticatedUser));
    }

    @PreAuthorize("hasRole('COMPANY_MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}