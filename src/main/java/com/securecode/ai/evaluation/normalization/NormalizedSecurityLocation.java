package com.securecode.ai.evaluation.normalization;

public record NormalizedSecurityLocation(
        String file,
        Integer startLine,
        Integer startColumn,
        Integer endLine,
        Integer endColumn
) { }
