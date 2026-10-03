package com.atsresume.resume;

import com.atsresume.auth.AuthenticatedUser;
import com.atsresume.common.ResourceNotFoundException;
import com.atsresume.jobposting.JobPostingService;
import com.atsresume.parsing.ResumeParser;
import com.atsresume.parsing.ResumeParserFactory;
import com.atsresume.storage.FileStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeScoreRepository resumeScoreRepository;
    private final ResumeSuggestionRepository resumeSuggestionRepository;
    private final FileStorageService fileStorageService;
    private final ResumeParserFactory parserFactory;
    private final JobPostingService jobPostingService;
    private final ResumeProcessingService resumeProcessingService;

    public ResumeService(ResumeRepository resumeRepository,
                          ResumeScoreRepository resumeScoreRepository,
                          ResumeSuggestionRepository resumeSuggestionRepository,
                          FileStorageService fileStorageService,
                          ResumeParserFactory parserFactory,
                          JobPostingService jobPostingService,
                          ResumeProcessingService resumeProcessingService) {
        this.resumeRepository = resumeRepository;
        this.resumeScoreRepository = resumeScoreRepository;
        this.resumeSuggestionRepository = resumeSuggestionRepository;
        this.fileStorageService = fileStorageService;
        this.parserFactory = parserFactory;
        this.jobPostingService = jobPostingService;
        this.resumeProcessingService = resumeProcessingService;
    }

    public ResumeUploadResponse upload(MultipartFile file, Long jobPostingId, String candidateName, AuthenticatedUser uploader) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume";
        // Validates the file type up front so unsupported formats fail the request immediately
        // instead of surfacing as an async failure the recruiter has to poll for.
        ResumeParser parser = parserFactory.parserFor(file.getContentType(), filename);

        if (jobPostingId != null) {
            jobPostingService.getEntityById(jobPostingId);
        }

        String storagePath = storeFile(file, filename);

        Resume resume = new Resume(candidateName, filename, storagePath, file.getContentType(), jobPostingId, uploader.id());
        resume = resumeRepository.save(resume);

        resumeProcessingService.process(resume.getId(), parser);

        return ResumeUploadResponse.from(resume);
    }

    private String storeFile(MultipartFile file, String filename) {
        String storageKey = UUID.randomUUID().toString();
        try (InputStream content = file.getInputStream()) {
            return fileStorageService.store(storageKey, filename, content);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded resume file", e);
        }
    }

    public Resume getEntityById(Long resumeId) {
        return resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found: " + resumeId));
    }

    public ResumeDetailResponse getDetail(Long resumeId) {
        Resume resume = getEntityById(resumeId);
        ResumeScore score = resumeScoreRepository.findByResumeId(resumeId).orElse(null);
        List<SuggestionResponse> suggestions = resumeSuggestionRepository.findByResumeId(resumeId).stream()
                .sorted(Comparator.comparingInt(s -> s.getPriority().ordinal()))
                .map(SuggestionResponse::from)
                .toList();
        return ResumeDetailResponse.from(resume, score, suggestions);
    }

    public Page<ResumeSummaryResponse> search(Long jobPostingId, ResumeStatus status, Integer minScore, Pageable pageable) {
        Page<Resume> resumes = resumeRepository.search(jobPostingId, status, minScore, pageable);
        List<Long> resumeIds = resumes.getContent().stream().map(Resume::getId).toList();
        Map<Long, Integer> scoresByResumeId = resumeScoreRepository.findByResumeIdIn(resumeIds).stream()
                .collect(Collectors.toMap(ResumeScore::getResumeId, ResumeScore::getOverallScore));
        return resumes.map(resume -> ResumeSummaryResponse.from(resume, scoresByResumeId.get(resume.getId())));
    }
}
