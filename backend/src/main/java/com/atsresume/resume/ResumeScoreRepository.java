package com.atsresume.resume;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ResumeScoreRepository extends JpaRepository<ResumeScore, Long> {

    Optional<ResumeScore> findByResumeId(Long resumeId);

    List<ResumeScore> findByResumeIdIn(Collection<Long> resumeIds);
}
