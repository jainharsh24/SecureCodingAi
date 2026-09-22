package com.securecode.ai.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record SubmissionRequest(@NotBlank @Size(max = 200000) String sourceCode) { }
