package com.securecode.ai.service;

import com.securecode.ai.dto.SecurityEvaluationResponse;
import com.securecode.ai.dto.SecurityFindingResponse;
import com.securecode.ai.entity.SecurityEvaluation;
import com.securecode.ai.entity.Submission;
import com.securecode.ai.evaluation.SecurityFinding;
import com.securecode.ai.evaluation.SemgrepClient;
import com.securecode.ai.evaluation.SemgrepScanResult;
import com.securecode.ai.evaluation.normalization.NormalizedSecurityResult;
import com.securecode.ai.evaluation.normalization.SemgrepEvidenceNormalizer;
import com.securecode.ai.repository.SecurityEvaluationRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SecurityEvaluationService {
    private static final String EVALUATOR = "SEMGREP";
    private final SemgrepClient semgrepClient;
    private final SecurityEvaluationRepository securityEvaluationRepository;
    private final SemgrepEvidenceNormalizer semgrepEvidenceNormalizer;

    public SecurityEvaluationService(SemgrepClient semgrepClient, SecurityEvaluationRepository securityEvaluationRepository,
            SemgrepEvidenceNormalizer semgrepEvidenceNormalizer) {
        this.semgrepClient = semgrepClient;
        this.securityEvaluationRepository = securityEvaluationRepository;
        this.semgrepEvidenceNormalizer = semgrepEvidenceNormalizer;
    }

    public SecurityEvaluationResponse evaluateJava(Submission submission, String sourceCode) {
        SemgrepScanResult result = semgrepClient.scanJava(sourceCode);
        NormalizedSecurityResult normalized = semgrepEvidenceNormalizer.normalize(result);
        SecurityFinding firstFinding = result.findings().isEmpty() ? null : result.findings().getFirst();
        String severity = firstFinding == null && normalized.status().name().equals("COMPLETED") ? "INFO"
                : firstFinding == null ? "EVALUATOR_ERROR" : firstFinding.severity();
        securityEvaluationRepository.save(new SecurityEvaluation(submission, EVALUATOR, result.detected(),
                firstFinding == null ? null : firstFinding.vulnerabilityType(), firstFinding == null ? null : firstFinding.cwe(),
                severity, null, result.rawJson() == null ? result.error() : result.rawJson()));
        return new SecurityEvaluationResponse(EVALUATOR, normalized.status().name(), !normalized.findings().isEmpty(),
                result.findings().stream().map(this::toResponse).toList(), normalized.error(), normalized);
    }

    private SecurityFindingResponse toResponse(SecurityFinding finding) {
        return new SecurityFindingResponse(finding.ruleId(), finding.message(), finding.severity(), finding.vulnerabilityType(),
                finding.cwe(), finding.startLine(), finding.code());
    }
}
