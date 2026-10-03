package com.atsresume.parsing;

import com.atsresume.common.UnsupportedFileTypeException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResumeParserFactory {

    private final List<ResumeParser> parsers;

    public ResumeParserFactory(List<ResumeParser> parsers) {
        this.parsers = parsers;
    }

    public ResumeParser parserFor(String contentType, String filename) {
        return parsers.stream()
                .filter(parser -> parser.supports(contentType, filename))
                .findFirst()
                .orElseThrow(() -> new UnsupportedFileTypeException(
                        "Unsupported resume file type. Only PDF and DOCX are supported: " + filename));
    }
}
