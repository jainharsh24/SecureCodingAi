package com.securecode.ai.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "judge0")
public record Judge0Properties(
        @NotBlank String baseUrl,
        String authToken,
        @Min(100) long pollIntervalMs,
        @Min(1000) long maxWaitMs
) { }
