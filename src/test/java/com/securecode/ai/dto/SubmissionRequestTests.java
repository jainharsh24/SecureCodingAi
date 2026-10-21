package com.securecode.ai.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SubmissionRequestTests {
    @Test
    void normalizesWindowsAndLegacyMacLineEndingsBeforeEvaluation() {
        SubmissionRequest request = new SubmissionRequest("class Main {\r\n  // demo\r}\n");

        assertThat(request.sourceCode()).isEqualTo("class Main {\n  // demo\n}\n");
    }
}
