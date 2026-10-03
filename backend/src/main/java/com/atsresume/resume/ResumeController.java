package com.atsresume.resume;

import com.atsresume.auth.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ResumeUploadResponse> upload(@RequestParam("file") MultipartFile file,
                                                         @RequestParam(value = "jobPostingId", required = false) Long jobPostingId,
                                                         @RequestParam(value = "candidateName", required = false) String candidateName,
                                                         @AuthenticationPrincipal AuthenticatedUser currentUser) {
        ResumeUploadResponse response = resumeService.upload(file, jobPostingId, candidateName, currentUser);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping
    public Page<ResumeSummaryResponse> search(@RequestParam(required = false) Long jobPostingId,
                                               @RequestParam(required = false) ResumeStatus status,
                                               @RequestParam(required = false) Integer minScore,
                                               @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return resumeService.search(jobPostingId, status, minScore, pageable);
    }

    @GetMapping("/{id}")
    public ResumeDetailResponse getDetail(@PathVariable Long id) {
        return resumeService.getDetail(id);
    }
}
