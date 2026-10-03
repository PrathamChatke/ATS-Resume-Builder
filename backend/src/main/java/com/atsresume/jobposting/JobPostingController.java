package com.atsresume.jobposting;

import com.atsresume.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-postings")
public class JobPostingController {

    private final JobPostingService jobPostingService;

    public JobPostingController(JobPostingService jobPostingService) {
        this.jobPostingService = jobPostingService;
    }

    @PostMapping
    public ResponseEntity<JobPostingResponse> create(@Valid @RequestBody CreateJobPostingRequest request,
                                                       @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobPostingService.create(request, currentUser));
    }

    @GetMapping
    public List<JobPostingResponse> listAll() {
        return jobPostingService.listAll();
    }

    @GetMapping("/{id}")
    public JobPostingResponse getById(@PathVariable Long id) {
        return jobPostingService.getById(id);
    }
}
