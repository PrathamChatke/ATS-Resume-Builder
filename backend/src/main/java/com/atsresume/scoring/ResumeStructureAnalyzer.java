package com.atsresume.scoring;

import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Detects the structural signals ATS systems and recruiters actually look
 * for: standard sections, contact details, quantified impact, and weak
 * passive phrasing. Pure text heuristics — no ML — so results are
 * deterministic and explainable.
 */
@Component
public class ResumeStructureAnalyzer {

    private static final Map<String, List<String>> SECTION_HEADERS = Map.of(
            "SUMMARY", List.of("summary", "professional summary", "career summary", "objective", "career objective", "profile"),
            "EXPERIENCE", List.of("experience", "work experience", "professional experience", "employment history", "work history"),
            "EDUCATION", List.of("education", "academic background", "academic qualifications"),
            "SKILLS", List.of("skills", "technical skills", "core competencies", "key skills", "areas of expertise")
    );

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(?:\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}\\b");

    private static final Pattern QUANTIFICATION_PATTERN =
            Pattern.compile("[%$₹€]|\\b\\d+([.,]\\d+)?\\b");

    private final Set<String> actionVerbs;
    private final Set<String> weakPhrases;

    public ResumeStructureAnalyzer() {
        this.actionVerbs = DictionaryLoader.load("dictionaries/action_verbs.txt");
        this.weakPhrases = DictionaryLoader.load("dictionaries/weak_phrases.txt");
    }

    public StructureFindings analyze(String text) {
        String lowerText = text.toLowerCase();
        String[] lines = text.split("\\r?\\n");

        Set<String> presentSections = detectSections(lines);
        Set<String> missingSections = new LinkedHashSet<>(SECTION_HEADERS.keySet());
        missingSections.removeAll(presentSections);

        int contentLineCount = 0;
        int quantifiedLineCount = 0;
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.length() < 20 || line.length() > 300 || isHeaderLine(line)) {
                continue;
            }
            contentLineCount++;
            if (QUANTIFICATION_PATTERN.matcher(line).find()) {
                quantifiedLineCount++;
            }
        }

        int actionVerbCount = countDistinctWordMatches(lowerText, actionVerbs);
        List<String> weakPhrasesFound = weakPhrases.stream()
                .filter(lowerText::contains)
                .toList();
        int weakPhraseOccurrences = weakPhrasesFound.stream()
                .mapToInt(phrase -> countOccurrences(lowerText, phrase))
                .sum();

        int wordCount = text.isBlank() ? 0 : text.trim().split("\\s+").length;

        var emailMatcher = EMAIL_PATTERN.matcher(text);
        String email = emailMatcher.find() ? emailMatcher.group() : null;
        var phoneMatcher = PHONE_PATTERN.matcher(text);
        String phone = phoneMatcher.find() ? phoneMatcher.group() : null;

        return new StructureFindings(
                presentSections,
                missingSections,
                email,
                phone,
                lowerText.contains("linkedin.com"),
                wordCount,
                contentLineCount,
                quantifiedLineCount,
                actionVerbCount,
                weakPhraseOccurrences,
                weakPhrasesFound,
                parseabilityRatio(text));
    }

    private Set<String> detectSections(String[] lines) {
        Set<String> present = new LinkedHashSet<>();
        for (String rawLine : lines) {
            String line = normalizeHeader(rawLine);
            if (line.isEmpty() || line.length() > 40) {
                continue;
            }
            for (var entry : SECTION_HEADERS.entrySet()) {
                if (!present.contains(entry.getKey()) && entry.getValue().contains(line)) {
                    present.add(entry.getKey());
                }
            }
        }
        return present;
    }

    private boolean isHeaderLine(String line) {
        String normalized = normalizeHeader(line);
        return SECTION_HEADERS.values().stream().anyMatch(synonyms -> synonyms.contains(normalized));
    }

    private String normalizeHeader(String line) {
        return line.trim().toLowerCase().replaceAll("[:\\-]+$", "").trim();
    }

    private int countDistinctWordMatches(String lowerText, Set<String> dictionary) {
        int count = 0;
        for (String word : dictionary) {
            if (lowerText.contains(" " + word) || lowerText.startsWith(word)) {
                count++;
            }
        }
        return count;
    }

    private int countOccurrences(String text, String phrase) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(phrase, index)) != -1) {
            count++;
            index += phrase.length();
        }
        return count;
    }

    private double parseabilityRatio(String text) {
        long total = text.chars().filter(c -> !Character.isWhitespace(c)).count();
        if (total == 0) {
            return 0.0;
        }
        long clean = text.chars()
                .filter(c -> Character.isLetterOrDigit(c) || ".,;:()%$#+/-@".indexOf(c) >= 0)
                .count();
        return (double) clean / total;
    }
}
