package com.atsresume.scoring;

import com.atsresume.config.ScoringProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AtsScoreCalculatorTest {

    private final ScoringProperties weights = new ScoringProperties(
            new ScoringProperties.Weights(0.35, 0.20, 0.20, 0.15, 0.10));

    private final AtsScoreCalculator calculator =
            new AtsScoreCalculator(new KeywordExtractor(), new ResumeStructureAnalyzer(), weights);

    private static final String STRONG_RESUME = """
            Jane Doe
            jane.doe@example.com | (415) 555-2671 | linkedin.com/in/janedoe

            Summary
            Backend engineer with 6 years of experience building distributed systems in Java and Kubernetes.

            Experience
            Senior Software Engineer, Example Corp
            - Architected and led the migration of the payments microservices to Kubernetes, cutting deploy time by 40%.
            - Reduced p99 latency by 25% by redesigning the Redis caching layer.
            - Automated the CI/CD pipeline with Jenkins, increasing release frequency by 3x.

            Education
            B.S. Computer Science, State University

            Skills
            Java, Spring Boot, PostgreSQL, Kubernetes, Docker, AWS, REST API, microservices
            """;

    private static final String WEAK_RESUME = """
            John Smith

            Was responsible for handling customer support tickets and worked on the internal wiki.
            Helped with onboarding new hires and assisted with various other duties.
            """;

    private static final String JOB_DESCRIPTION = """
            We are looking for a Senior Software Engineer with strong Java and Spring Boot experience,
            hands-on knowledge of Kubernetes, Docker and AWS, and a track record of building
            scalable microservices and REST APIs.
            """;

    @Test
    void strongResumeScoresHigherThanWeakResume() {
        ScoreResult strong = calculator.calculate(STRONG_RESUME, JOB_DESCRIPTION);
        ScoreResult weak = calculator.calculate(WEAK_RESUME, JOB_DESCRIPTION);

        assertThat(strong.overallScore()).isGreaterThan(weak.overallScore());
        assertThat(strong.overallScore()).isBetween(0, 100);
        assertThat(weak.overallScore()).isBetween(0, 100);
    }

    @Test
    void matchesKeywordsAgainstJobDescription() {
        ScoreResult result = calculator.calculate(STRONG_RESUME, JOB_DESCRIPTION);

        assertThat(result.matchedKeywords()).contains("java", "spring boot", "kubernetes", "docker", "aws");
        assertThat(result.missingKeywords()).isEmpty();
    }

    @Test
    void flagsMissingKeywordsWhenResumeLacksThem() {
        ScoreResult result = calculator.calculate(WEAK_RESUME, JOB_DESCRIPTION);

        assertThat(result.missingKeywords()).contains("java", "spring boot", "kubernetes");
    }

    @Test
    void fallsBackToGenericScoringWithoutJobDescription() {
        ScoreResult result = calculator.calculate(STRONG_RESUME, null);

        assertThat(result.overallScore()).isBetween(0, 100);
        assertThat(result.missingKeywords()).isEmpty();
        assertThat(result.matchedKeywords()).isNotEmpty();
    }

    @Test
    void categoryScoresAreAllWithinRange() {
        ScoreResult result = calculator.calculate(STRONG_RESUME, JOB_DESCRIPTION);

        result.categoryScores().values().forEach(score -> assertThat(score).isBetween(0, 100));
        assertThat(result.categoryScores()).containsKeys(
                ScoreCategory.KEYWORD_MATCH, ScoreCategory.FORMATTING, ScoreCategory.STRUCTURE,
                ScoreCategory.IMPACT, ScoreCategory.CONTACT_INFO);
    }
}
