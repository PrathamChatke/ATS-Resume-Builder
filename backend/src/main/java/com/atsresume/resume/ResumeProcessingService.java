package com.atsresume.resume;

import com.atsresume.common.ResourceNotFoundException;
import com.atsresume.jobposting.JobPostingService;
import com.atsresume.parsing.ParsedResume;
import com.atsresume.parsing.ResumeParser;
import com.atsresume.scoring.AtsScoreCalculator;
import com.atsresume.scoring.ScoreCategory;
import com.atsresume.scoring.ScoreResult;
import com.atsresume.storage.FileStorageService;
import com.atsresume.suggestion.ResumeFindings;
import com.atsresume.suggestion.SuggestionGenerator;
import com.atsresume.suggestion.SuggestionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs the parse-score-suggest pipeline off the request thread. Kept as a
 * separate bean (not a method on {@link ResumeService}) so the {@code @Async}
 * call from the upload endpoint goes through Spring's proxy instead of being
 * silently executed inline via self-invocation.
 */
@Service
public class ResumeProcessingService {

    private static final Logger log = LoggerFactory.getLogger(ResumeProcessingService.class);

    private final ResumeRepository resumeRepository;
    private final ResumeScoreRepository resumeScoreRepository;
    private final ResumeSuggestionRepository resumeSuggestionRepository;
    private final FileStorageService fileStorageService;
    private final AtsScoreCalculator scoreCalculator;
    private final SuggestionGenerator suggestionGenerator;
    private final JobPostingService jobPostingService;

    public ResumeProcessingService(ResumeRepository resumeRepository,
                                    ResumeScoreRepository resumeScoreRepository,
                                    ResumeSuggestionRepository resumeSuggestionRepository,
                                    FileStorageService fileStorageService,
                                    AtsScoreCalculator scoreCalculator,
                                    SuggestionGenerator suggestionGenerator,
                                    JobPostingService jobPostingService) {
        this.resumeRepository = resumeRepository;
        this.resumeScoreRepository = resumeScoreRepository;
        this.resumeSuggestionRepository = resumeSuggestionRepository;
        this.fileStorageService = fileStorageService;
        this.scoreCalculator = scoreCalculator;
        this.suggestionGenerator = suggestionGenerator;
        this.jobPostingService = jobPostingService;
    }

    @Async("resumeProcessingExecutor")
    public void process(Long resumeId, ResumeParser parser) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found: " + resumeId));
        try {
            String resumeText = extractText(resume, parser);
            if (resumeText.isBlank()) {
                failResume(resumeId, "Could not extract any readable text from the uploaded file.");
                return;
            }

            String jobDescription = resume.getJobPostingId() != null
                    ? jobPostingService.getEntityById(resume.getJobPostingId()).getDescription()
                    : null;

            ScoreResult scoreResult = scoreCalculator.calculate(resumeText, jobDescription);
            resumeScoreRepository.save(toEntity(resumeId, scoreResult));

            List<SuggestionItem> suggestions = generateSuggestionsSafely(resumeId, scoreResult);
            resumeSuggestionRepository.saveAll(suggestions.stream()
                    .map(s -> new ResumeSuggestion(resumeId, s.category(), s.priority(), s.text()))
                    .toList());

            resume.markProcessed(scoreResult.structureFindings().email());
            resumeRepository.save(resume);
        } catch (Exception e) {
            log.error("Resume processing failed for resume {}", resumeId, e);
            failResume(resumeId, "Processing failed: " + e.getMessage());
        }
    }

    private List<SuggestionItem> generateSuggestionsSafely(Long resumeId, ScoreResult scoreResult) {
        try {
            return suggestionGenerator.generate(ResumeFindings.from(scoreResult));
        } catch (RuntimeException e) {
            log.warn("Suggestion generation failed for resume {}; continuing without suggestions.", resumeId, e);
            return List.of();
        }
    }

    private String extractText(Resume resume, ResumeParser parser) {
        try (InputStream content = fileStorageService.load(resume.getStoragePath())) {
            ParsedResume parsed = parser.parse(content);
            return parsed.text() != null ? parsed.text() : "";
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read stored resume file", e);
        }
    }

    private void failResume(Long resumeId, String reason) {
        resumeRepository.findById(resumeId).ifPresent(resume -> {
            resume.markFailed(reason);
            resumeRepository.save(resume);
        });
    }

    private ResumeScore toEntity(Long resumeId, ScoreResult scoreResult) {
        Map<String, Integer> categoryScores = new LinkedHashMap<>();
        for (Map.Entry<ScoreCategory, Integer> entry : scoreResult.categoryScores().entrySet()) {
            categoryScores.put(entry.getKey().name(), entry.getValue());
        }
        return new ResumeScore(resumeId, scoreResult.overallScore(), categoryScores,
                List.copyOf(scoreResult.matchedKeywords()), List.copyOf(scoreResult.missingKeywords()));
    }
}
