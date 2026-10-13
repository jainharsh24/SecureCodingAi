package com.securecode.ai.service;

import com.securecode.ai.dto.SecurityEvaluationResponse;
import com.securecode.ai.dto.SecurityFindingResponse;
import com.securecode.ai.entity.SecurityEvaluation;
import com.securecode.ai.entity.Submission;
import com.securecode.ai.evaluation.SecurityFinding;
import com.securecode.ai.evaluation.semgrep.SemgrepClient;
import com.securecode.ai.evaluation.semgrep.SemgrepScanResult;
import com.securecode.ai.evaluation.common.SecurityFindingEvidenceCodec;
import com.securecode.ai.evaluation.semgrep.normalization.NormalizedSecurityResult;
import com.securecode.ai.evaluation.semgrep.normalization.SemgrepEvidenceNormalizer;
import com.securecode.ai.evaluation.spotbugs.SpotBugsClient;
import com.securecode.ai.evaluation.spotbugs.SpotBugsScanResult;
import com.securecode.ai.repository.SecurityEvaluationRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SecurityEvaluationService {
    private static final String SEMGREP = "SEMGREP";
    private static final String SPOTBUGS = "SPOTBUGS";
    private final SemgrepClient semgrepClient;
    private final SpotBugsClient spotBugsClient;
    private final SecurityEvaluationRepository securityEvaluationRepository;
    private final SemgrepEvidenceNormalizer semgrepEvidenceNormalizer;
    private final SecurityFindingEvidenceCodec findingEvidenceCodec;

    public SecurityEvaluationService(SemgrepClient semgrepClient, SecurityEvaluationRepository securityEvaluationRepository,
            SemgrepEvidenceNormalizer semgrepEvidenceNormalizer, SpotBugsClient spotBugsClient,
            SecurityFindingEvidenceCodec findingEvidenceCodec) {
        this.semgrepClient = semgrepClient;
        this.securityEvaluationRepository = securityEvaluationRepository;
        this.semgrepEvidenceNormalizer = semgrepEvidenceNormalizer;
        this.spotBugsClient = spotBugsClient;
        this.findingEvidenceCodec = findingEvidenceCodec;
    }

    public SecurityEvaluationResponse evaluateJava(Submission submission, String sourceCode) {
        SemgrepScanResult result = semgrepClient.scanJava(sourceCode);
        NormalizedSecurityResult normalized = semgrepEvidenceNormalizer.normalize(result);
        persist(submission, SEMGREP, result.findings(), normalized.status().name(), normalized.error());

        SpotBugsScanResult spotBugsResult = spotBugsClient.scanJava(sourceCode);
        persist(submission, SPOTBUGS, spotBugsResult.findings(), spotBugsResult.status().name(), spotBugsResult.error());

        // Keep the established submission response shape: it remains the Semgrep response.
        return new SecurityEvaluationResponse(SEMGREP, normalized.status().name(), !normalized.findings().isEmpty(),
                result.findings().stream().map(this::toResponse).toList(), normalized.error(), normalized);
    }

    private void persist(Submission submission, String evaluator, List<SecurityFinding> findings, String status, String error) {
        SecurityFinding firstFinding = findings.isEmpty() ? null : findings.getFirst();
        String severity = firstFinding == null && "COMPLETED".equals(status) ? "INFO"
                : firstFinding == null ? "EVALUATOR_ERROR" : firstFinding.severity();
        securityEvaluationRepository.save(new SecurityEvaluation(submission, evaluator, !findings.isEmpty(),
                firstFinding == null ? null : firstFinding.vulnerabilityType(), firstFinding == null ? null : firstFinding.cwe(),
                severity, null, findingEvidenceCodec.encode(findings), status, error));
    }

    private SecurityFindingResponse toResponse(SecurityFinding finding) {
        return new SecurityFindingResponse(finding.ruleId(), finding.message(), finding.severity(), finding.vulnerabilityType(),
                finding.cwe(), finding.startLine(), finding.code());
    }
}
