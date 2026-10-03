package com.atsresume.parsing;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

@Component
public class DocxResumeParser implements ResumeParser {

    @Override
    public boolean supports(String contentType, String filename) {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equalsIgnoreCase(contentType)
                || filename.toLowerCase().endsWith(".docx");
    }

    @Override
    public ParsedResume parse(InputStream content) {
        try (XWPFDocument document = new XWPFDocument(content);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return new ParsedResume(extractor.getText());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse DOCX resume", e);
        }
    }
}
