package com.cagri.hrms.controller;

import com.cagri.hrms.dto.request.employee.EmployeeCreateRequestDTO;
import com.cagri.hrms.dto.request.employee.EmployeeRequestDTO;
import com.cagri.hrms.dto.response.employee.EmployeeResponseDTO;
import com.cagri.hrms.mapper.EmployeeMapper;
import com.cagri.hrms.service.EmployeeService;
import com.cagri.hrms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeMapper employeeMapper;
    private final UserService userService;


    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @RequestBody EmployeeCreateRequestDTO createDTO,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails // <-- burası değişti
    ) {
        String email = userDetails.getUsername();

        EmployeeRequestDTO internalDTO = employeeMapper.toRequestDTO(createDTO);
        internalDTO.setUserId(userService.getUserIdByEmail(email));
        internalDTO.setCompanyId(userService.getCompanyIdByEmail(email));

        return ResponseEntity.ok(employeeService.createEmployee(internalDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable Long id,
            @RequestBody @Valid EmployeeCreateRequestDTO updateDTO,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User userDetails
    ) {
        String email = userDetails.getUsername();

        EmployeeRequestDTO internalDTO = employeeMapper.toRequestDTO(updateDTO);
        internalDTO.setUserId(userService.getUserIdByEmail(email));
        internalDTO.setCompanyId(userService.getCompanyIdByEmail(email));

        return ResponseEntity.ok(employeeService.updateEmployee(id, internalDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
