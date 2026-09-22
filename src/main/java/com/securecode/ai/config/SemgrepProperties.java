package com.securecode.ai.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "semgrep")
public record SemgrepProperties(
        boolean enabled,
        @NotBlank String wslDistribution,
        @NotBlank String executable,
        @NotBlank String rulesPath,
        @Min(1000) long timeoutMs
) { }
