package com.atsresume.resume;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeSuggestionRepository extends JpaRepository<ResumeSuggestion, Long> {

    List<ResumeSuggestion> findByResumeId(Long resumeId);
}
