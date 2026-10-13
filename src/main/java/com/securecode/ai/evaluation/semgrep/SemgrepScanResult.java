package com.securecode.ai.evaluation.semgrep;

import com.securecode.ai.evaluation.SecurityEvaluationStatus;
import com.securecode.ai.evaluation.SecurityFinding;
import java.util.List;

public record SemgrepScanResult(
        SecurityEvaluationStatus status,
        List<SecurityFinding> findings,
        String rawJson,
        String error
) {
    public boolean detected() { return !findings.isEmpty(); }
}
