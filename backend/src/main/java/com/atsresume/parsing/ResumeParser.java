package com.atsresume.parsing;

import java.io.InputStream;

public interface ResumeParser {

    boolean supports(String contentType, String filename);

    ParsedResume parse(InputStream content);
}
