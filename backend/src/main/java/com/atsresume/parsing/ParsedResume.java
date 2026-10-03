package com.atsresume.parsing;

/**
 * Plain text extracted from a resume file, ready for structural and keyword
 * analysis. Line breaks from the source document are preserved since section
 * detection relies on them.
 */
public record ParsedResume(String text) {
}
