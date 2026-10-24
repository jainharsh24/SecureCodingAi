package com.securecode.ai.evaluation.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Bounded execution controls for independent post-Judge0 security evaluators. */
@Validated
@ConfigurationProperties(prefix = "evaluation.security")
public record SecurityEvaluationProperties(
        @Min(1) int concurrency,
        @Min(1000) long timeoutMs) { }
