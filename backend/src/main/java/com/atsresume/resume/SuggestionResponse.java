package com.atsresume.resume;

import com.atsresume.suggestion.Priority;

public record SuggestionResponse(String category, Priority priority, String text) {

    public static SuggestionResponse from(ResumeSuggestion suggestion) {
        return new SuggestionResponse(suggestion.getCategory(), suggestion.getPriority(), suggestion.getSuggestionText());
    }
}
