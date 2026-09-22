package com.securecode.ai.evaluation.normalization;

import com.securecode.ai.evaluation.SecurityFinding;
import com.securecode.ai.evaluation.SemgrepScanResult;
import org.springframework.stereotype.Component;

@Component
public class SemgrepEvidenceNormalizer implements EvidenceNormalizer<SemgrepScanResult> {
    public static final String EVALUATOR = "SEMGREP";

    @Override
    public NormalizedSecurityResult normalize(SemgrepScanResult result) {
        return new NormalizedSecurityResult(EVALUATOR, result.status(), result.findings().stream()
                .map(this::normalizeFinding)
                .toList(), result.error());
    }

    private NormalizedSecurityFinding normalizeFinding(SecurityFinding finding) {
        return new NormalizedSecurityFinding(true, finding.vulnerabilityType(), finding.cwe(), finding.severity(), null,
                finding.code(), new NormalizedSecurityLocation(finding.file(), finding.startLine(), finding.startColumn(),
                        finding.endLine(), finding.endColumn()));
    }
}
