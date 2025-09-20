package com.cagri.hrms.repository;

import com.cagri.hrms.entity.core.Review;
import com.cagri.hrms.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Landing/public
    List<Review> findTop50ByStatusOrderByPublishedAtDesc(ReviewStatus status);

    // Admin/Panel + general dashboard search (optional filters)
    @Query("""
        select r from Review r
        where (:companyId is null or r.company.id = :companyId)
          and (:status    is null or r.status = :status)
          and (:authorId  is null or r.author.id = :authorId)
        order by r.createdAt desc
        """)
    Page<Review> search(@Param("companyId") Long companyId,
                        @Param("status") ReviewStatus status,
                        @Param("authorId") Long authorId,
                        Pageable pageable);

    // (Optional) direct author listing – currently unused but useful for tests
    Page<Review> findByAuthor_IdOrderByCreatedAtDesc(Long authorId, Pageable pageable);
}
