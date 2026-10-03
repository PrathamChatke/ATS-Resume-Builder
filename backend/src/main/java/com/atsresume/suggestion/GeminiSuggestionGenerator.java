package com.atsresume.suggestion;

import com.atsresume.config.GeminiProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Turns the scoring engine's raw findings into professional, MAANG-style
 * suggestion text. The LLM only phrases what the deterministic engine already
 * found - it does not compute the score and is asked not to invent facts.
 */
@Component
public class GeminiSuggestionGenerator implements SuggestionGenerator {

    private static final String SYSTEM_INSTRUCTION = """
            You are a senior technical recruiter and resume coach at a top-tier
            ("MAANG-level") technology company. You are given the deterministic
            findings from an ATS-scoring engine for one resume. Write improvement
            suggestions the candidate can act on immediately.

            Rules:
            - Base every suggestion strictly on the findings provided. Do not invent
              facts about the candidate that are not present in the findings.
            - Write in a professional, direct, constructive tone, the way a senior
              recruiter at a leading tech company would - no fluff, no generic advice.
            - Each suggestion must be specific and actionable: what to change and why.
            - Order suggestions so the ones most likely to raise the ATS score come first.
            - Return between 3 and 8 suggestions.
            """;

    private final Client geminiClient;
    private final GeminiProperties properties;
    private final ObjectMapper objectMapper;

    public GeminiSuggestionGenerator(Client geminiClient, GeminiProperties properties, ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    @Retry(name = "gemini")
    @Bulkhead(name = "gemini")
    public List<SuggestionItem> generate(ResumeFindings findings) {
        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(Content.fromParts(Part.fromText(SYSTEM_INSTRUCTION)))
                .responseMimeType("application/json")
                .responseSchema(responseSchema())
                .temperature(0.4f)
                .build();

        GenerateContentResponse response = geminiClient.models.generateContent(
                properties.model(), buildFindingsPrompt(findings), config);

        return parseSuggestions(response.text());
    }

    private Schema responseSchema() {
        Schema item = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of(
                        "category", Schema.builder().type(Type.Known.STRING).build(),
                        "priority", Schema.builder().type(Type.Known.STRING).enum_("HIGH", "MEDIUM", "LOW").build(),
                        "text", Schema.builder().type(Type.Known.STRING).build()))
                .required(List.of("category", "priority", "text"))
                .build();
        return Schema.builder().type(Type.Known.ARRAY).items(item).build();
    }

    private String buildFindingsPrompt(ResumeFindings findings) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Overall ATS score: ").append(findings.overallScore()).append("/100\n");
        prompt.append("Category scores (0-100 each): ").append(findings.categoryScores()).append('\n');
        prompt.append("Missing keywords versus the target role: ")
                .append(joinOrNone(findings.missingKeywords())).append('\n');
        prompt.append("Missing standard resume sections: ")
                .append(joinOrNone(findings.missingSections())).append('\n');
        prompt.append("Weak/passive phrases found: ").append(joinOrNone(findings.weakPhrasesFound()))
                .append(" (").append(findings.weakPhraseOccurrences()).append(" total occurrences)\n");
        prompt.append("Strong action verbs already used: ").append(findings.actionVerbCount()).append('\n');
        prompt.append("Has email: ").append(findings.hasEmail())
                .append(", has phone: ").append(findings.hasPhone())
                .append(", has LinkedIn URL: ").append(findings.hasLinkedIn()).append('\n');
        prompt.append("Resume word count: ").append(findings.wordCount()).append('\n');
        return prompt.toString();
    }

    private String joinOrNone(Iterable<String> values) {
        String joined = String.join(", ", values);
        return joined.isEmpty() ? "none" : joined;
    }

    private List<SuggestionItem> parseSuggestions(String json) {
        try {
            List<RawSuggestion> raw = objectMapper.readValue(json, new TypeReference<>() {
            });
            return raw.stream()
                    .map(r -> new SuggestionItem(r.category(), Priority.valueOf(r.priority()), r.text()))
                    .toList();
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new SuggestionGenerationException("Gemini returned a response that could not be parsed as suggestions", e);
        }
    }

    private record RawSuggestion(String category, String priority, String text) {
    }
}
