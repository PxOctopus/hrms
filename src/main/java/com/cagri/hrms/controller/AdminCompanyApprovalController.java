package com.cagri.hrms.controller;

import com.cagri.hrms.dto.response.company.CompanyResponseDTO;
import com.cagri.hrms.dto.response.user.PendingManagerDTO;
import com.cagri.hrms.service.CompanyService;
import com.cagri.hrms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
public class AdminCompanyApprovalController {

    private final CompanyService companyService;
    private final UserService userService;

    // List all managers whose companies are waiting for admin approval
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<List<PendingManagerDTO>> getPendingManagers() {
        List<PendingManagerDTO> pendingManagers = userService.getPendingManagers();
        return ResponseEntity.ok(pendingManagers);
    }

    // Approve the company request from a manager and create the company
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/approve/{userId}")
    public ResponseEntity<CompanyResponseDTO> approveCompany(@PathVariable Long userId) {
        CompanyResponseDTO createdCompany = companyService.approvePendingCompany(userId);
        return ResponseEntity.ok(createdCompany);
    }

    // Reject the company request from a manager, delete the user and notify via email
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reject/{userId}")
    public ResponseEntity<Void> rejectCompany(@PathVariable Long userId) {
        userService.rejectPendingManager(userId);
        return ResponseEntity.ok().build();
    }
}
