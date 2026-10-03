package com.atsresume.scoring;

import java.util.List;
import java.util.Set;

public record StructureFindings(
        Set<String> presentSections,
        Set<String> missingSections,
        String email,
        String phone,
        boolean hasLinkedIn,
        int wordCount,
        int contentLineCount,
        int quantifiedLineCount,
        int actionVerbCount,
        int weakPhraseOccurrences,
        List<String> weakPhrasesFound,
        double parseabilityRatio) {

    public boolean hasEmail() {
        return email != null;
    }

    public boolean hasPhone() {
        return phone != null;
    }
}
