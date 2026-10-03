package com.atsresume.scoring;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeStructureAnalyzerTest {

    private final ResumeStructureAnalyzer analyzer = new ResumeStructureAnalyzer();

    @Test
    void detectsStandardSectionsAndContactInfo() {
        String resume = """
                Jane Doe
                jane.doe@example.com | (415) 555-2671 | linkedin.com/in/janedoe

                Summary
                Backend engineer with 6 years of experience building distributed systems.

                Experience
                Senior Software Engineer, Example Corp
                - Led migration of the payments service to Kubernetes, cutting deploy time by 40%.
                - Reduced p99 latency by 25% by redesigning the caching layer.

                Education
                B.S. Computer Science, State University

                Skills
                Java, Spring Boot, PostgreSQL, Kubernetes
                """;

        StructureFindings findings = analyzer.analyze(resume);

        assertThat(findings.presentSections()).containsExactlyInAnyOrder("SUMMARY", "EXPERIENCE", "EDUCATION", "SKILLS");
        assertThat(findings.missingSections()).isEmpty();
        assertThat(findings.hasEmail()).isTrue();
        assertThat(findings.hasPhone()).isTrue();
        assertThat(findings.hasLinkedIn()).isTrue();
        assertThat(findings.quantifiedLineCount()).isGreaterThan(0);
    }

    @Test
    void flagsMissingSectionsAndWeakPhrasing() {
        String resume = """
                John Smith

                Was responsible for handling customer support tickets and worked on the internal wiki.
                Helped with onboarding new hires.
                """;

        StructureFindings findings = analyzer.analyze(resume);

        assertThat(findings.presentSections()).isEmpty();
        assertThat(findings.missingSections()).containsExactlyInAnyOrder("SUMMARY", "EXPERIENCE", "EDUCATION", "SKILLS");
        assertThat(findings.hasEmail()).isFalse();
        assertThat(findings.weakPhrasesFound()).contains("responsible for", "worked on", "helped with");
        assertThat(findings.weakPhraseOccurrences()).isGreaterThanOrEqualTo(3);
    }
}
