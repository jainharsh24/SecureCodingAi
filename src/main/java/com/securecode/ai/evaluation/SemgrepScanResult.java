package com.securecode.ai.evaluation;

import java.util.List;

public record SemgrepScanResult(
        SecurityEvaluationStatus status,
        List<SecurityFinding> findings,
        String rawJson,
        String error
) {
    public boolean detected() { return !findings.isEmpty(); }
}
