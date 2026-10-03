package com.atsresume.jobposting;

import com.atsresume.auth.AuthenticatedUser;
import com.atsresume.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;

    public JobPostingService(JobPostingRepository jobPostingRepository) {
        this.jobPostingRepository = jobPostingRepository;
    }

    public JobPostingResponse create(CreateJobPostingRequest request, AuthenticatedUser createdBy) {
        JobPosting jobPosting = new JobPosting(request.title(), request.description(), createdBy.id());
        return JobPostingResponse.from(jobPostingRepository.save(jobPosting));
    }

    public List<JobPostingResponse> listAll() {
        return jobPostingRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(JobPostingResponse::from)
                .toList();
    }

    public JobPosting getEntityById(Long id) {
        return jobPostingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found: " + id));
    }

    public JobPostingResponse getById(Long id) {
        return JobPostingResponse.from(getEntityById(id));
    }
}
