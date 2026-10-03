package com.atsresume.scoring;

import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Reads a plain-text, one-entry-per-line dictionary from the classpath.
 * Blank lines and '#' comments are ignored. Used for the curated skills,
 * action-verb and weak-phrase lists so they can be tuned without a rebuild
 * of the scoring logic itself.
 */
final class DictionaryLoader {

    private DictionaryLoader() {
    }

    static Set<String> load(String classpathLocation) {
        Set<String> entries = new LinkedHashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource(classpathLocation).getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    entries.add(trimmed.toLowerCase());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load dictionary: " + classpathLocation, e);
        }
        return entries;
    }
}
