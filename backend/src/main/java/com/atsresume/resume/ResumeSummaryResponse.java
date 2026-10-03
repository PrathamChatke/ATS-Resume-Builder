package com.atsresume.resume;

import java.time.Instant;

public record ResumeSummaryResponse(
        Long id,
        String candidateName,
        String candidateEmail,
        String originalFilename,
        ResumeStatus status,
        Long jobPostingId,
        Integer overallScore,
        Instant createdAt) {

    public static ResumeSummaryResponse from(Resume resume, Integer overallScore) {
        return new ResumeSummaryResponse(
                resume.getId(),
                resume.getCandidateName(),
                resume.getCandidateEmail(),
                resume.getOriginalFilename(),
                resume.getStatus(),
                resume.getJobPostingId(),
                overallScore,
                resume.getCreatedAt());
    }
}
