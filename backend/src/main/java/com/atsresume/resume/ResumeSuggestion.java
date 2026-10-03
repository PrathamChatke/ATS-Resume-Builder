package com.atsresume.resume;

import com.atsresume.suggestion.Priority;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "resume_suggestions")
public class ResumeSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resume_id", nullable = false)
    private Long resumeId;

    @Column(nullable = false, length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priority priority;

    @Column(name = "suggestion_text", nullable = false, columnDefinition = "text")
    private String suggestionText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ResumeSuggestion() {
    }

    public ResumeSuggestion(Long resumeId, String category, Priority priority, String suggestionText) {
        this.resumeId = resumeId;
        this.category = category;
        this.priority = priority;
        this.suggestionText = suggestionText;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public String getCategory() {
        return category;
    }

    public Priority getPriority() {
        return priority;
    }

    public String getSuggestionText() {
        return suggestionText;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
