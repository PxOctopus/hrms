package com.cagri.hrms.service.impl;

import com.cagri.hrms.dto.request.review.CreateReviewRequest;
import com.cagri.hrms.dto.response.review.PagedResponse;
import com.cagri.hrms.dto.response.review.ReviewPublicDTO;
import com.cagri.hrms.dto.response.review.ReviewRowDTO;
import com.cagri.hrms.entity.core.Company;
import com.cagri.hrms.entity.core.Review;
import com.cagri.hrms.entity.core.User;
import com.cagri.hrms.enums.ReviewStatus;
import com.cagri.hrms.repository.ReviewRepository;
import com.cagri.hrms.service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository repo;

    // Create a new review with status PENDING (Manager submits it)
    @Override
    public void createPending(User manager, Company company, CreateReviewRequest req) {
        Review r = new Review();
        r.setAuthor(manager);
        r.setCompany(company);
        r.setTitle(Optional.ofNullable(req.title()).orElse("")); // fallback to empty string if null
        r.setContent(req.content());
        r.setRating(req.rating() == null ? 5 : req.rating());    // default rating = 5
        r.setStatus(ReviewStatus.PENDING);
        repo.save(r);
    }

    // Fetch latest public reviews for landing page showcase
    @Override
    public List<ReviewPublicDTO> listPublic() {
        return repo.findTop50ByStatusOrderByPublishedAtDesc(ReviewStatus.PUBLISHED)
                .stream()
                .map(r -> new ReviewPublicDTO(
                        r.getId(),
                        r.getAuthor().getFullName(),
                        r.getContent(),
                        r.safeRating(),
                        r.getCreatedAt()
                ))
                .toList();
    }

    // Fetch reviews for dashboard (supports company/status/author filters and pagination)
    @Override
    public PagedResponse<ReviewRowDTO> search(Long companyId, ReviewStatus status,
                                              Long authorId, int page, int size) {
        Page<Review> p = repo.search(
                companyId, status, authorId,
                PageRequest.of(Math.max(0, page - 1), Math.max(1, size))
        );

        List<ReviewRowDTO> rows = p.getContent().stream()
                .map(r -> new ReviewRowDTO(
                        r.getId(),
                        nullToDash(r.getTitle()),
                        r.getContent(),
                        r.safeRating(),
                        r.getAuthor().getFullName(),
                        r.getStatus(),
                        r.getCreatedAt(),
                        r.getRejectionReason()
                ))
                .toList();

        return new PagedResponse<>(rows, p.getTotalElements(), page, size);
    }

    // Admin approves a review -> mark as PUBLISHED
    @Override
    @Transactional
    public void approve(Long id) {
        Review r = repo.findById(id).orElseThrow();
        r.setStatus(ReviewStatus.PUBLISHED);
        r.setRejectionReason(null);       // clear any previous rejection reason
        r.setPublishedAt(Instant.now());
    }

    // Admin rejects a review -> mark as REJECTED and store reason
    @Override
    @Transactional
    public void reject(Long id, String reason) {
        Review r = repo.findById(id).orElseThrow();
        r.setStatus(ReviewStatus.REJECTED);
        r.setRejectionReason(Objects.requireNonNullElse(reason, "Reason not specified."));
    }

    // Utility to replace null/blank strings with dash
    private static String nullToDash(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}
