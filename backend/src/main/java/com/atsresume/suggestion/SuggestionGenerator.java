package com.atsresume.suggestion;

import java.util.List;

public interface SuggestionGenerator {

    List<SuggestionItem> generate(ResumeFindings findings);
}
