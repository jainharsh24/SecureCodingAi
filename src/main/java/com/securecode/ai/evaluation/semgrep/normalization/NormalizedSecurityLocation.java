package com.securecode.ai.evaluation.semgrep.normalization;

public record NormalizedSecurityLocation(
        String file,
        Integer startLine,
        Integer startColumn,
        Integer endLine,
        Integer endColumn
) { }
