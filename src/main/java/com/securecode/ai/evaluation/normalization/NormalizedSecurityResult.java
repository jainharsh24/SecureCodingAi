package com.securecode.ai.evaluation.normalization;

import com.securecode.ai.evaluation.SecurityEvaluationStatus;
import java.util.List;

public record NormalizedSecurityResult(
        String evaluator,
        SecurityEvaluationStatus status,
        List<NormalizedSecurityFinding> findings,
        String error
) {
    public NormalizedSecurityResult {
        findings = List.copyOf(findings);
    }
}
