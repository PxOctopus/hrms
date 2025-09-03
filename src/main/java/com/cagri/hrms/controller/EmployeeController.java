package com.cagri.hrms.controller;

import com.cagri.hrms.dto.request.employee.EmployeeCreateRequestDTO;
import com.cagri.hrms.dto.request.employee.EmployeeUpdateProfileRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeMeDTO;
import com.cagri.hrms.dto.response.employee.EmployeeResponseDTO;
import com.cagri.hrms.dto.response.general.MessageResponseDTO;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.service.EmployeeService;
import com.cagri.hrms.service.UserService;
import com.cagri.hrms.util.UserValidator;
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

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @RequestBody @Valid EmployeeCreateRequestDTO createDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User authenticatedUser = userService.getUserByEmail(userDetails.getUsername());
        UserValidator.assertCompanyApproved(authenticatedUser);
        return ResponseEntity.ok(employeeService.createEmployee(createDTO, authenticatedUser));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id,
                                                               @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        UserValidator.assertCompanyApproved(user);
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        UserValidator.assertCompanyApproved(user);
        return ResponseEntity.ok(employeeService.getAllEmployees(user));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable Long id,
            @RequestBody @Valid EmployeeCreateRequestDTO updateDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User authenticatedUser = userService.getUserByEmail(userDetails.getUsername());
        UserValidator.assertCompanyApproved(authenticatedUser);
        return ResponseEntity.ok(employeeService.updateEmployee(id, updateDTO, authenticatedUser));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        UserValidator.assertCompanyApproved(user);
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<EmployeeResponseDTO> toggleEmployeeActiveStatus(@PathVariable Long id,
                                                                          @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        UserValidator.assertCompanyApproved(user);

        // Return updated employee info
        EmployeeResponseDTO updatedEmployee = employeeService.toggleStatus(id);
        return ResponseEntity.ok(updatedEmployee);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PutMapping("/my-profile")
    public ResponseEntity<EmployeeResponseDTO> updateOwnProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid EmployeeUpdateProfileRequestDTO dto
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        EmployeeResponseDTO updated = employeeService.updateOwnProfile(currentUser.getId(), dto);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/me")
    public ResponseEntity<EmployeeMeDTO> me(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(employeeService.getMine(currentUser));
    }
    }
