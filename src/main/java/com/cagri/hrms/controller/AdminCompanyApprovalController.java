package com.cagri.hrms.controller;

import com.cagri.hrms.dto.response.company.CompanyResponseDTO;
import com.cagri.hrms.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
public class AdminCompanyApprovalController {

    private final CompanyService companyService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/approve/{userId}")
        public ResponseEntity<CompanyResponseDTO> approveCompany(@PathVariable Long userId) {
        CompanyResponseDTO createdCompany = companyService.approvePendingCompany(userId);
        return ResponseEntity.ok(createdCompany);
    }
}
