package com.securecode.ai.evaluation.common;

import com.securecode.ai.entity.SecurityEvaluation;

/** Aggregates only normalized, valid evaluator verdicts. */
public record SecurityEvidenceSummary(int safeCount, int vulnerableCount) {
    public static SecurityEvidenceSummary from(Iterable<SecurityEvaluation> evaluations) {
        int safe = 0;
        int vulnerable = 0;
        for (SecurityEvaluation evaluation : evaluations) {
            var normalized = NormalizedEvaluatorEvidence.from(evaluation);
            if (normalized.isEmpty()) continue;
            if (normalized.get().verdict() == NormalizedEvaluatorEvidence.Verdict.SAFE) safe++;
            else vulnerable++;
        }
        return new SecurityEvidenceSummary(safe, vulnerable);
    }

    public int validEvaluatorCount() { return safeCount + vulnerableCount; }
    public int safePercent() { return percent(safeCount); }
    public int vulnerabilityPercent() { return validEvaluatorCount() == 0 ? 0 : 100 - safePercent(); }

    // Computing one rounded side and deriving the other guarantees a 100% total.
    private int percent(int count) {
        return validEvaluatorCount() == 0 ? 0 : Math.round(count * 100f / validEvaluatorCount());
    }
}
