package com.securecode.ai.evaluation;

public record SecurityFinding(
        String ruleId,
        String message,
        String severity,
        String vulnerabilityType,
        String cwe,
        String file,
        Integer startLine,
        Integer startColumn,
        Integer endLine,
        Integer endColumn,
        String code
) { }
