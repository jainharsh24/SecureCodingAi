package com.securecode.ai.evaluation.groq.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated @ConfigurationProperties(prefix = "groq")
public record GroqProperties(boolean enabled, @NotBlank String baseUrl, @NotBlank String model, String apiKey,
        @Min(1000) long timeoutMs, @Min(1) int maxOutputTokens, @Min(1024) int maxResponseChars,
        @Min(0) int maxRetries, boolean structuredOutput) { }
