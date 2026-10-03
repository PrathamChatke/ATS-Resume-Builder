package com.atsresume.scoring;

import com.atsresume.config.ScoringProperties;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Combines keyword matching and structural analysis into a single weighted
 * ATS score. Every sub-score is 0-100; weights (externalized in
 * application.yml) decide how much each contributes to the overall score.
 */
@Component
public class AtsScoreCalculator {

    private static final int GENERIC_KEYWORD_TARGET = 10;
    private static final int MIN_HEALTHY_WORD_COUNT = 150;
    private static final int MAX_HEALTHY_WORD_COUNT = 1200;

    private final KeywordExtractor keywordExtractor;
    private final ResumeStructureAnalyzer structureAnalyzer;
    private final ScoringProperties scoringProperties;

    public AtsScoreCalculator(KeywordExtractor keywordExtractor,
                               ResumeStructureAnalyzer structureAnalyzer,
                               ScoringProperties scoringProperties) {
        this.keywordExtractor = keywordExtractor;
        this.structureAnalyzer = structureAnalyzer;
        this.scoringProperties = scoringProperties;
    }

    public ScoreResult calculate(String resumeText, String jobDescriptionText) {
        StructureFindings structure = structureAnalyzer.analyze(resumeText);

        Set<String> matchedKeywords;
        Set<String> missingKeywords;
        int keywordScore;
        if (jobDescriptionText != null && !jobDescriptionText.isBlank()) {
            KeywordMatchResult match = keywordExtractor.matchAgainstJobDescription(resumeText, jobDescriptionText);
            matchedKeywords = match.matched();
            missingKeywords = match.missing();
            keywordScore = percentage(match.matched().size(), match.jobSkills().size());
        } else {
            Set<String> resumeSkills = keywordExtractor.extractSkills(resumeText);
            matchedKeywords = resumeSkills;
            missingKeywords = Set.of();
            keywordScore = percentage(Math.min(resumeSkills.size(), GENERIC_KEYWORD_TARGET), GENERIC_KEYWORD_TARGET);
        }

        int totalCoreSections = structure.presentSections().size() + structure.missingSections().size();
        int structureScore = percentage(structure.presentSections().size(), totalCoreSections);
        int contactScore = contactInfoScore(structure);
        int impactScore = impactScore(structure);
        int formattingScore = formattingScore(structure);

        Map<ScoreCategory, Integer> categoryScores = new EnumMap<>(ScoreCategory.class);
        categoryScores.put(ScoreCategory.KEYWORD_MATCH, keywordScore);
        categoryScores.put(ScoreCategory.STRUCTURE, structureScore);
        categoryScores.put(ScoreCategory.CONTACT_INFO, contactScore);
        categoryScores.put(ScoreCategory.IMPACT, impactScore);
        categoryScores.put(ScoreCategory.FORMATTING, formattingScore);

        int overall = weightedOverall(categoryScores);

        return new ScoreResult(overall, categoryScores, matchedKeywords, missingKeywords, structure);
    }

    private int weightedOverall(Map<ScoreCategory, Integer> scores) {
        ScoringProperties.Weights w = scoringProperties.weights();
        double total = scores.get(ScoreCategory.KEYWORD_MATCH) * w.keywordMatch()
                + scores.get(ScoreCategory.FORMATTING) * w.formatting()
                + scores.get(ScoreCategory.STRUCTURE) * w.structure()
                + scores.get(ScoreCategory.IMPACT) * w.impact()
                + scores.get(ScoreCategory.CONTACT_INFO) * w.contactInfo();
        return clamp((int) Math.round(total));
    }

    private int contactInfoScore(StructureFindings s) {
        double score = 0;
        if (s.hasEmail()) {
            score += 50;
        }
        if (s.hasPhone()) {
            score += 35;
        }
        if (s.hasLinkedIn()) {
            score += 15;
        }
        return clamp((int) Math.round(score));
    }

    private int impactScore(StructureFindings s) {
        if (s.contentLineCount() == 0) {
            return 0;
        }
        double quantifiedRatio = (double) s.quantifiedLineCount() / s.contentLineCount();
        int base = (int) Math.round(quantifiedRatio * 100);
        int weakPhrasePenalty = Math.min(40, s.weakPhraseOccurrences() * 10);
        return clamp(base - weakPhrasePenalty);
    }

    private int formattingScore(StructureFindings s) {
        int lengthScore;
        if (s.wordCount() < MIN_HEALTHY_WORD_COUNT) {
            lengthScore = percentage(s.wordCount(), MIN_HEALTHY_WORD_COUNT);
        } else if (s.wordCount() > MAX_HEALTHY_WORD_COUNT) {
            int excessHundreds = (s.wordCount() - MAX_HEALTHY_WORD_COUNT) / 20;
            lengthScore = clamp(100 - excessHundreds);
        } else {
            lengthScore = 100;
        }
        int parseScore = clamp((int) Math.round(s.parseabilityRatio() * 100));
        return (lengthScore + parseScore) / 2;
    }

    private int percentage(int numerator, int denominator) {
        if (denominator <= 0) {
            return 100;
        }
        return clamp((int) Math.round(100.0 * numerator / denominator));
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
