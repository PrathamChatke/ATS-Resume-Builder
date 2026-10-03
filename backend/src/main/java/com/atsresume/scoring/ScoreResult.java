package com.atsresume.scoring;

import java.util.Map;
import java.util.Set;

public record ScoreResult(
        int overallScore,
        Map<ScoreCategory, Integer> categoryScores,
        Set<String> matchedKeywords,
        Set<String> missingKeywords,
        StructureFindings structureFindings) {
}
