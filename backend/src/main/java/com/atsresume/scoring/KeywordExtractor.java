package com.atsresume.scoring;

import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Finds curated skill/technology keywords in resume or job-description text.
 * Matching is plain substring lookup against a maintained dictionary rather
 * than NLP/ML — deterministic, fast, and easy for the client to extend by
 * editing {@code dictionaries/skills.txt}.
 */
@Component
public class KeywordExtractor {

    private final Set<String> skillDictionary;

    public KeywordExtractor() {
        this.skillDictionary = DictionaryLoader.load("dictionaries/skills.txt");
    }

    public Set<String> extractSkills(String text) {
        String normalized = normalize(text);
        Set<String> found = new LinkedHashSet<>();
        for (String skill : skillDictionary) {
            if (normalized.contains(" " + skill + " ")) {
                found.add(skill);
            }
        }
        return found;
    }

    public KeywordMatchResult matchAgainstJobDescription(String resumeText, String jobDescriptionText) {
        Set<String> resumeSkills = extractSkills(resumeText);
        Set<String> jobSkills = extractSkills(jobDescriptionText);

        Set<String> matched = new LinkedHashSet<>(resumeSkills);
        matched.retainAll(jobSkills);

        Set<String> missing = new LinkedHashSet<>(jobSkills);
        missing.removeAll(resumeSkills);

        return new KeywordMatchResult(resumeSkills, jobSkills, matched, missing);
    }

    private String normalize(String text) {
        String cleaned = text.toLowerCase().replaceAll("[^a-z0-9+#./\\s]", " ").replaceAll("\\s+", " ");
        return " " + cleaned + " ";
    }
}
