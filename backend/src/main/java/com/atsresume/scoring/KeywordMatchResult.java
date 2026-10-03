package com.atsresume.scoring;

import java.util.Set;

public record KeywordMatchResult(
        Set<String> resumeSkills,
        Set<String> jobSkills,
        Set<String> matched,
        Set<String> missing) {
}
