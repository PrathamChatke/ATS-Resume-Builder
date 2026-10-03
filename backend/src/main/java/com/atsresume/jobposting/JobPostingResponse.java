package com.atsresume.jobposting;

import java.time.Instant;

public record JobPostingResponse(Long id, String title, String description, Instant createdAt) {

    public static JobPostingResponse from(JobPosting jobPosting) {
        return new JobPostingResponse(jobPosting.getId(), jobPosting.getTitle(), jobPosting.getDescription(), jobPosting.getCreatedAt());
    }
}
