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

/**
 * Shift definitions controller (name + daily time window).
 * Company scope always derives from the authenticated user.
 */
@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;
    private final UserService userService;

    /** Managers only: create a shift for their own company (DTO.companyId is ignored). */
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<ShiftResponseDTO> createShift(
            @Valid @RequestBody ShiftRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(shiftService.createShift(dto, currentUser));
    }

    /** Managers only: update a shift that belongs to their company. */
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

    /** Managers only: delete a shift that belongs to their company. */
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

    /** Authenticated users: read single shift (must belong to same company). */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ShiftResponseDTO> getShiftById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(shiftService.getShiftById(id, currentUser));
    }

    /** Authenticated users: list all shifts of the current user's company. */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<ShiftResponseDTO>> getMyCompanyShifts(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(shiftService.getMyCompanyShifts(currentUser));
    }
}
