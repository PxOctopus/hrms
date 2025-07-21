package com.cagri.hrms.controller;

import com.cagri.hrms.dto.request.employee.BreakRequestDTO;
import com.cagri.hrms.dto.response.employee.BreakResponseDTO;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.service.BreakService;
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
@RequestMapping("/api/breaks")
@RequiredArgsConstructor
public class BreakController {

    private final BreakService breakService;
    private final UserService userService;

    // Only users with MANAGER role can create a break
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<BreakResponseDTO> createBreak(
            @Valid @RequestBody BreakRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        BreakResponseDTO createdBreak = breakService.createBreak(requestDTO, currentUser);
        return ResponseEntity.ok(createdBreak);
    }

    // Update break: restricted to MANAGER
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<BreakResponseDTO> updateBreak(
            @PathVariable Long id,
            @Valid @RequestBody BreakRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        BreakResponseDTO updatedBreak = breakService.updateBreak(id, requestDTO, currentUser);
        return ResponseEntity.ok(updatedBreak);
    }

    // Delete break: restricted to MANAGER
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBreak(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        breakService.deleteBreak(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    // Get all breaks: restricted to MANAGER role only
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<List<BreakResponseDTO>> getAllBreaks(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        List<BreakResponseDTO> breaks = breakService.getAllBreaks(currentUser);
        return ResponseEntity.ok(breaks);
    }

    // Get breaks by employee ID: accessible to MANAGER or the employee themselves
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<BreakResponseDTO>> getBreaksByEmployeeId(
            @PathVariable Long employeeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        List<BreakResponseDTO> breaks = breakService.getBreaksByEmployeeId(employeeId, currentUser);
        return ResponseEntity.ok(breaks);
    }
}
