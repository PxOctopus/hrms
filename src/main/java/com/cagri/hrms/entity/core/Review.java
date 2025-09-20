package com.cagri.hrms.entity.core;

import com.cagri.hrms.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    private String title;
    @Column(columnDefinition = "text")
    private String content;

    private Integer rating;

    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(columnDefinition = "text")
    private String rejectionReason;

    private Instant createdAt = Instant.now();
    private Instant publishedAt;

    // convenience getter
    public int safeRating(){ return Math.min(5, Math.max(1, rating == null ? 5 : rating)); }
}
