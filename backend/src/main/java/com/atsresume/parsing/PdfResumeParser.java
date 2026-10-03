package com.atsresume.parsing;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

@Component
public class PdfResumeParser implements ResumeParser {

    @Override
    public boolean supports(String contentType, String filename) {
        return "application/pdf".equalsIgnoreCase(contentType) || filename.toLowerCase().endsWith(".pdf");
    }

    @Override
    public ParsedResume parse(InputStream content) {
        try (PDDocument document = Loader.loadPDF(content.readAllBytes())) {
            String text = new PDFTextStripper().getText(document);
            return new ParsedResume(text);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse PDF resume", e);
        }
    }
}
