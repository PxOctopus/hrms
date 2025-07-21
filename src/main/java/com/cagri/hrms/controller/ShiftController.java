package com.cagri.hrms.controller;

import com.cagri.hrms.dto.request.employee.ShiftRequestDTO;
import com.cagri.hrms.dto.response.employee.ShiftResponseDTO;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.service.ShiftService;
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
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;
    private final UserService userService;

    // Only MANAGER can create a shift
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<ShiftResponseDTO> createShift(
            @Valid @RequestBody ShiftRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(shiftService.createShift(dto, currentUser));
    }

    // Only MANAGER can update a shift
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ShiftResponseDTO> updateShift(
            @PathVariable Long id,
            @Valid @RequestBody ShiftRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(shiftService.updateShift(id, dto, currentUser));
    }

    // Only MANAGER can delete a shift
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShift(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        shiftService.deleteShift(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    // Get all shifts for a company
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<ShiftResponseDTO>> getShiftsByCompanyId(
            @PathVariable Long companyId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(shiftService.getShiftsByCompanyId(companyId, currentUser));
    }

    // Get all shifts
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<ShiftResponseDTO>> getAllShifts(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(shiftService.getAllShifts(currentUser));
    }

    // Get a shift by ID
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ShiftResponseDTO> getShiftById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(shiftService.getShiftById(id, currentUser));
    }
}

