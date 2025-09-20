package com.cagri.hrms.controller;

import com.cagri.hrms.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final ReviewService service;

    // Admin endpoint: approve a pending review -> status becomes PUBLISHED
    @PatchMapping("/{id}/approve")
    public void approve(@PathVariable Long id) {
        service.approve(id);
    }

    // Request body for reject action (admin should provide a reason)
    public static record RejectRequest(String reason) {}

    // Admin endpoint: reject a review -> status becomes REJECTED, reason is stored
    @PatchMapping("/{id}/reject")
    public void reject(@PathVariable Long id, @RequestBody RejectRequest body) {
        service.reject(id, body == null ? null : body.reason());
    }
}
