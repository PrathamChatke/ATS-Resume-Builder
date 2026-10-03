package com.atsresume.resume;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ResumeDetailResponse(
        Long id,
        String candidateName,
        String candidateEmail,
        String originalFilename,
        ResumeStatus status,
        String failureReason,
        Long jobPostingId,
        Integer overallScore,
        Map<String, Integer> categoryScores,
        List<String> matchedKeywords,
        List<String> missingKeywords,
        List<SuggestionResponse> suggestions,
        Instant createdAt,
        Instant processedAt) {

    public static ResumeDetailResponse from(Resume resume, ResumeScore score, List<SuggestionResponse> suggestions) {
        return new ResumeDetailResponse(
                resume.getId(),
                resume.getCandidateName(),
                resume.getCandidateEmail(),
                resume.getOriginalFilename(),
                resume.getStatus(),
                resume.getFailureReason(),
                resume.getJobPostingId(),
                score != null ? score.getOverallScore() : null,
                score != null ? score.getCategoryScores() : Map.of(),
                score != null ? score.getMatchedKeywords() : List.of(),
                score != null ? score.getMissingKeywords() : List.of(),
                suggestions,
                resume.getCreatedAt(),
                resume.getProcessedAt());
    }
}
