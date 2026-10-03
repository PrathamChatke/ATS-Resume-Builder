package com.atsresume.resume;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    @Query("""
            SELECT r FROM Resume r LEFT JOIN ResumeScore s ON s.resumeId = r.id
            WHERE (:jobPostingId IS NULL OR r.jobPostingId = :jobPostingId)
              AND (:status IS NULL OR r.status = :status)
              AND (:minScore IS NULL OR s.overallScore >= :minScore)
            """)
    Page<Resume> search(@Param("jobPostingId") Long jobPostingId,
                         @Param("status") ResumeStatus status,
                         @Param("minScore") Integer minScore,
                         Pageable pageable);
}
