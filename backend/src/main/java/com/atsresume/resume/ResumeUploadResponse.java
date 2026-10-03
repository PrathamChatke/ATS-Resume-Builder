package com.atsresume.resume;

public record ResumeUploadResponse(Long id, ResumeStatus status) {

    public static ResumeUploadResponse from(Resume resume) {
        return new ResumeUploadResponse(resume.getId(), resume.getStatus());
    }
}
