package com.atsresume.suggestion;

import com.atsresume.scoring.ScoreCategory;
import com.atsresume.scoring.ScoreResult;
import com.atsresume.scoring.StructureFindings;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The facts the deterministic scoring engine produced about one resume,
 * shaped as input for an LLM to turn into professionally worded advice.
 * The LLM never invents facts or scores here — it only phrases these.
 */
public record ResumeFindings(
        int overallScore,
        Map<ScoreCategory, Integer> categoryScores,
        Set<String> missingKeywords,
        Set<String> missingSections,
        int weakPhraseOccurrences,
        List<String> weakPhrasesFound,
        int actionVerbCount,
        boolean hasEmail,
        boolean hasPhone,
        boolean hasLinkedIn,
        int wordCount) {

    public static ResumeFindings from(ScoreResult scoreResult) {
        StructureFindings structure = scoreResult.structureFindings();
        return new ResumeFindings(
                scoreResult.overallScore(),
                scoreResult.categoryScores(),
                scoreResult.missingKeywords(),
                structure.missingSections(),
                structure.weakPhraseOccurrences(),
                structure.weakPhrasesFound(),
                structure.actionVerbCount(),
                structure.hasEmail(),
                structure.hasPhone(),
                structure.hasLinkedIn(),
                structure.wordCount());
    }
}
