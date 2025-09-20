package com.cagri.hrms.controller;

import com.cagri.hrms.dto.request.review.CreateReviewRequest;
import com.cagri.hrms.dto.response.review.PagedResponse;
import com.cagri.hrms.dto.response.review.ReviewPublicDTO;
import com.cagri.hrms.dto.response.review.ReviewRowDTO;
import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.enums.ReviewStatus;
import com.cagri.hrms.service.AuthService;
import com.cagri.hrms.service.CompanyService;
import com.cagri.hrms.service.ReviewService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;
    private final CompanyService companyService; // Resolve manager's company
    private final AuthService auth;              // Current authenticated user

    // Public endpoint: fetch published reviews for landing page showcase
    @GetMapping("/public")
    @PermitAll
    public List<ReviewPublicDTO> listPublic() {
        return service.listPublic();
    }

    // Dashboard endpoint: search reviews with optional filters (company, status) and pagination
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public PagedResponse<ReviewRowDTO> search(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        // Uses the overload without authorId (delegates to authorId=null)
        return service.search(companyId, status, page, pageSize);
    }

    // Manager endpoint: submit a new review (status will be set to PENDING)
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> create(@Valid @RequestBody CreateReviewRequest req) {
        User me = auth.getCurrentUser();                       // current principal
        Company c = companyService.getCurrentCompanyOrThrow(); // ensure a company context
        service.createPending(me, c, req);
        return ResponseEntity.ok().build();
    }

    // Manager endpoint: fetch all reviews created by the current manager
    @GetMapping("/mine")
    @PreAuthorize("hasRole('MANAGER')")
    public PagedResponse<ReviewRowDTO> myReviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        User me = auth.getCurrentUser();
        // Filter by authorId using the unified search
        return service.search(null, null, me.getId(), page, pageSize);
    }
}
