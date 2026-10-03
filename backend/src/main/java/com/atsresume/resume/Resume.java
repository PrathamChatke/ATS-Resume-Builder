package com.atsresume.resume;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidate_name")
    private String candidateName;

    @Column(name = "candidate_email")
    private String candidateEmail;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "job_posting_id")
    private Long jobPostingId;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResumeStatus status;

    @Column(name = "failure_reason", columnDefinition = "text")
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected Resume() {
    }

    public Resume(String candidateName, String originalFilename, String storagePath, String contentType,
                  Long jobPostingId, Long uploadedBy) {
        this.candidateName = candidateName;
        this.originalFilename = originalFilename;
        this.storagePath = storagePath;
        this.contentType = contentType;
        this.jobPostingId = jobPostingId;
        this.uploadedBy = uploadedBy;
        this.status = ResumeStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void markProcessed(String detectedEmail) {
        if (this.candidateEmail == null) {
            this.candidateEmail = detectedEmail;
        }
        this.status = ResumeStatus.PROCESSED;
        this.processedAt = Instant.now();
    }

    public void markFailed(String reason) {
        this.status = ResumeStatus.FAILED;
        this.failureReason = reason;
        this.processedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getJobPostingId() {
        return jobPostingId;
    }

    public Long getUploadedBy() {
        return uploadedBy;
    }

    public ResumeStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
