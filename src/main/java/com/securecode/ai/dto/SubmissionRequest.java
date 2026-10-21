package com.securecode.ai.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record SubmissionRequest(@NotBlank @Size(max = 200000) String sourceCode) {
    public SubmissionRequest {
        sourceCode = sourceCode == null ? null : sourceCode.replace("\r\n", "\n").replace('\r', '\n');
    }
}
