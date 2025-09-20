package com.cagri.hrms.service;

import com.cagri.hrms.dto.request.review.CreateReviewRequest;
import com.cagri.hrms.dto.response.review.PagedResponse;
import com.cagri.hrms.dto.response.review.ReviewPublicDTO;
import com.cagri.hrms.dto.response.review.ReviewRowDTO;
import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.enums.ReviewStatus;

import java.util.List;

public interface ReviewService {

    // Manager creates a new review (status = PENDING)
    void createPending(User manager, Company company, CreateReviewRequest req);

    // List published reviews for public showcase (landing page)
    List<ReviewPublicDTO> listPublic();

    // Unified search with optional filters (company, status, author)
    PagedResponse<ReviewRowDTO> search(Long companyId, ReviewStatus status,
                                       Long authorId, int page, int size);

    // Backward-compat convenience overload (no author filter)
    default PagedResponse<ReviewRowDTO> search(Long companyId, ReviewStatus status,
                                               int page, int size) {
        return search(companyId, status, null, page, size);
    }

    // Admin approves a review
    void approve(Long id);

    // Admin rejects a review with a reason
    void reject(Long id, String reason);
}
