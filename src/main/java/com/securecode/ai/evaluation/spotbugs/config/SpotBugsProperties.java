package com.securecode.ai.evaluation.spotbugs.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "spotbugs")
public record SpotBugsProperties(
        boolean enabled,
        @NotBlank String executable,
        @NotBlank String findSecBugsPluginPath,
        @NotBlank String javacExecutable,
        @Min(1000) long timeoutMs
) { }
