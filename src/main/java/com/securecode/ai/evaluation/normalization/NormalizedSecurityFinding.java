package com.securecode.ai.evaluation.normalization;

public record NormalizedSecurityFinding(
        boolean detected,
        String vulnerability,
        String cwe,
        String severity,
        Double confidence,
        String evidence,
        NormalizedSecurityLocation location
) { }
