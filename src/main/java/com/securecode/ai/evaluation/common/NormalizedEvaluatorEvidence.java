package com.securecode.ai.evaluation.common;

import com.securecode.ai.entity.SecurityEvaluation;
import java.util.Locale;
import java.util.Optional;

/**
 * The single interpretation point for persisted security evaluator results.
 * A completed process is evidence only when it also has a verdict the product
 * understands. This deliberately keeps transport/status information separate
 * from the SAFE/VULNERABLE evidence used in percentages.
 */
public record NormalizedEvaluatorEvidence(String evaluator, String status, Verdict verdict) {
    public enum Verdict { SAFE, VULNERABLE }

    public static Optional<NormalizedEvaluatorEvidence> from(SecurityEvaluation evaluation) {
        if (evaluation == null || evaluation.getEvaluatorName() == null) return Optional.empty();
        String evaluator = evaluation.getEvaluatorName().toUpperCase(Locale.ROOT);
        if (!evaluator.equals("SEMGREP") && !evaluator.equals("SPOTBUGS") && !evaluator.equals("GROQ")) {
            return Optional.empty();
        }
        String status = evaluation.getEvaluatorStatus();
        if (!"COMPLETED".equals(status)) return Optional.empty();
        if (evaluation.getEvaluatorError() != null && !evaluation.getEvaluatorError().isBlank()) return Optional.empty();

        if (evaluator.equals("GROQ")) {
            if ("SAFE".equals(evaluation.getAssessment())) {
                return Optional.of(new NormalizedEvaluatorEvidence(evaluator, status, Verdict.SAFE));
            }
            if ("VULNERABLE".equals(evaluation.getAssessment())) {
                return Optional.of(new NormalizedEvaluatorEvidence(evaluator, status, Verdict.VULNERABLE));
            }
            return Optional.empty(); // Uncertain, malformed, and legacy LLM replies are not a verdict.
        }
        return Optional.of(new NormalizedEvaluatorEvidence(evaluator, status,
                evaluation.isDetected() ? Verdict.VULNERABLE : Verdict.SAFE));
    }
}
