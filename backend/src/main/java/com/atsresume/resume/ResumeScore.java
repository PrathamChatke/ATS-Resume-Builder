package com.atsresume.resume;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "resume_scores")
public class ResumeScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resume_id", nullable = false, unique = true)
    private Long resumeId;

    @Column(name = "overall_score", nullable = false)
    private int overallScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "category_scores", nullable = false, columnDefinition = "jsonb")
    private Map<String, Integer> categoryScores;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matched_keywords", nullable = false, columnDefinition = "jsonb")
    private List<String> matchedKeywords;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "missing_keywords", nullable = false, columnDefinition = "jsonb")
    private List<String> missingKeywords;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ResumeScore() {
    }

    public ResumeScore(Long resumeId, int overallScore, Map<String, Integer> categoryScores,
                        List<String> matchedKeywords, List<String> missingKeywords) {
        this.resumeId = resumeId;
        this.overallScore = overallScore;
        this.categoryScores = categoryScores;
        this.matchedKeywords = matchedKeywords;
        this.missingKeywords = missingKeywords;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public int getOverallScore() {
        return overallScore;
    }

    public Map<String, Integer> getCategoryScores() {
        return categoryScores;
    }

    public List<String> getMatchedKeywords() {
        return matchedKeywords;
    }

    public List<String> getMissingKeywords() {
        return missingKeywords;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
