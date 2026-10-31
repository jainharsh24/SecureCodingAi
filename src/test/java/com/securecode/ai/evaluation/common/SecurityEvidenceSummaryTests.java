package com.securecode.ai.evaluation.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.securecode.ai.entity.SecurityEvaluation;
import java.util.List;
import org.junit.jupiter.api.Test;

class SecurityEvidenceSummaryTests {

    @Test
    void calculatesOneAndTwoAvailableSafeEvaluatorsAsFullSafeEvidence() {
        assertSummary(List.of(safe("SEMGREP")), 1, 0, 100, 0);
        assertSummary(List.of(safe("SEMGREP"), safe("SPOTBUGS")), 2, 0, 100, 0);
    }

    @Test
    void calculatesMixedEvidenceAndRoundsToOneHundredPercent() {
        assertSummary(List.of(safe("SEMGREP"), vulnerable("SPOTBUGS")), 1, 1, 50, 50);
        assertSummary(List.of(safe("SEMGREP"), safe("SPOTBUGS"), vulnerable("GROQ")), 2, 1, 67, 33);
    }

    @Test
    void calculatesThreeAvailableVulnerableEvaluatorsAsFullVulnerabilityEvidence() {
        assertSummary(List.of(vulnerable("SEMGREP"), vulnerable("SPOTBUGS"), vulnerable("GROQ")), 0, 3, 0, 100);
    }

    @Test
    void excludesUnavailableTimeoutMalformedAndInconclusiveResults() {
        SecurityEvaluation unavailable = evaluation("SPOTBUGS", false, "NOT_CONFIGURED", null);
        SecurityEvaluation timeout = evaluation("SEMGREP", false, "TIMEOUT", null);
        SecurityEvaluation uncertainGroq = evaluation("GROQ", false, "COMPLETED", "UNCERTAIN");
        SecurityEvaluation malformedGroq = evaluation("GROQ", false, "COMPLETED", null);

        assertSummary(List.of(safe("SEMGREP"), unavailable, timeout, uncertainGroq, malformedGroq), 1, 0, 100, 0);
        assertSummary(List.of(unavailable, timeout, uncertainGroq, malformedGroq), 0, 0, 0, 0);
    }

    private static void assertSummary(List<SecurityEvaluation> evaluations, int safe, int vulnerable, int safePercent,
            int vulnerablePercent) {
        SecurityEvidenceSummary summary = SecurityEvidenceSummary.from(evaluations);
        assertThat(summary.safeCount()).isEqualTo(safe);
        assertThat(summary.vulnerableCount()).isEqualTo(vulnerable);
        assertThat(summary.validEvaluatorCount()).isEqualTo(safe + vulnerable);
        assertThat(summary.safePercent()).isEqualTo(safePercent);
        assertThat(summary.vulnerabilityPercent()).isEqualTo(vulnerablePercent);
        assertThat(summary.safePercent() + summary.vulnerabilityPercent()).isEqualTo(summary.validEvaluatorCount() == 0 ? 0 : 100);
    }

    private static SecurityEvaluation safe(String evaluator) {
        return evaluation(evaluator, false, "COMPLETED", "GROQ".equals(evaluator) ? "SAFE" : null);
    }

    private static SecurityEvaluation vulnerable(String evaluator) {
        return evaluation(evaluator, true, "COMPLETED", "GROQ".equals(evaluator) ? "VULNERABLE" : null);
    }

    private static SecurityEvaluation evaluation(String evaluator, boolean detected, String status, String assessment) {
        return new SecurityEvaluation(null, evaluator, detected, null, null, detected ? "HIGH" : "INFO", null, "{}", status,
                null, assessment, null, null);
    }
}
