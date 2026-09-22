package com.securecode.ai.evaluation.normalization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.securecode.ai.evaluation.SecurityEvaluationStatus;
import com.securecode.ai.evaluation.SecurityFinding;
import com.securecode.ai.evaluation.SemgrepScanResult;
import java.util.List;
import org.junit.jupiter.api.Test;

class SemgrepEvidenceNormalizerTests {
    private final SemgrepEvidenceNormalizer normalizer = new SemgrepEvidenceNormalizer();

    @Test
    void normalizesDetectedFindingWithLocation() {
        var result = normalizer.normalize(scan(SecurityEvaluationStatus.COMPLETED, List.of(commandInjectionFinding()), null));

        assertEquals("SEMGREP", result.evaluator());
        assertEquals(SecurityEvaluationStatus.COMPLETED, result.status());
        assertEquals(1, result.findings().size());
        var finding = result.findings().getFirst();
        assertTrue(finding.detected());
        assertEquals("COMMAND_INJECTION", finding.vulnerability());
        assertEquals("CWE-78", finding.cwe());
        assertEquals("ERROR", finding.severity());
        assertNull(finding.confidence());
        assertEquals("Runtime.getRuntime().exec(command);", finding.evidence());
        assertEquals("Main.java", finding.location().file());
        assertEquals(7, finding.location().startLine());
        assertEquals(5, finding.location().startColumn());
        assertEquals(7, finding.location().endLine());
        assertEquals(44, finding.location().endColumn());
    }

    @Test
    void preservesCompletedStatusWhenNoFindingsExist() {
        var result = normalizer.normalize(scan(SecurityEvaluationStatus.COMPLETED, List.of(), null));

        assertEquals(SecurityEvaluationStatus.COMPLETED, result.status());
        assertTrue(result.findings().isEmpty());
        assertNull(result.error());
    }

    @Test
    void preservesEvaluatorErrorWithoutTreatingItAsCleanResult() {
        var result = normalizer.normalize(scan(SecurityEvaluationStatus.ERROR, List.of(), "Semgrep executable was unavailable"));

        assertEquals(SecurityEvaluationStatus.ERROR, result.status());
        assertTrue(result.findings().isEmpty());
        assertEquals("Semgrep executable was unavailable", result.error());
    }

    @Test
    void preservesMultipleFindingsWithoutDeduplication() {
        var sqlInjection = new SecurityFinding("sql-rule", "Unsafe SQL", "ERROR", "SQL_INJECTION", "CWE-89",
                "Main.java", 12, 9, 12, 51, "statement.executeQuery(sql);");
        var result = normalizer.normalize(scan(SecurityEvaluationStatus.COMPLETED,
                List.of(commandInjectionFinding(), sqlInjection), null));

        assertEquals(2, result.findings().size());
        assertEquals("COMMAND_INJECTION", result.findings().get(0).vulnerability());
        assertEquals("SQL_INJECTION", result.findings().get(1).vulnerability());
        assertFalse(result.findings().isEmpty());
    }

    private SemgrepScanResult scan(SecurityEvaluationStatus status, List<SecurityFinding> findings, String error) {
        return new SemgrepScanResult(status, findings, "{}", error);
    }

    private SecurityFinding commandInjectionFinding() {
        return new SecurityFinding("command-rule", "Unsafe command execution", "ERROR", "COMMAND_INJECTION", "CWE-78",
                "Main.java", 7, 5, 7, 44, "Runtime.getRuntime().exec(command);");
    }
}
