package com.securecode.ai.service;

import com.securecode.ai.dto.AssessmentResponse;
import com.securecode.ai.dto.EvaluatorResultResponse;
import com.securecode.ai.dto.EvidenceSummaryResponse;
import com.securecode.ai.dto.SubmissionAnalysisResponse;
import com.securecode.ai.entity.FunctionalEvaluation;
import com.securecode.ai.entity.FunctionalEvaluationStatus;
import com.securecode.ai.entity.SecurityEvaluation;
import com.securecode.ai.entity.Submission;
import com.securecode.ai.repository.FunctionalEvaluationRepository;
import com.securecode.ai.repository.SecurityEvaluationRepository;
import com.securecode.ai.repository.SubmissionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Builds the read model consumed by the analysis page from persisted evaluator outputs. */
@Service
public class SubmissionAnalysisService {
    private static final List<EvaluatorDefinition> EVALUATOR_CATALOG = List.of(
            new EvaluatorDefinition("JUDGE0", "Judge0"),
            new EvaluatorDefinition("SEMGREP", "Semgrep"),
            new EvaluatorDefinition("PMD", "PMD"),
            new EvaluatorDefinition("SPOTBUGS", "SpotBugs"),
            new EvaluatorDefinition("LLM", "LLM"));

    private final SubmissionRepository submissionRepository;
    private final FunctionalEvaluationRepository functionalEvaluationRepository;
    private final SecurityEvaluationRepository securityEvaluationRepository;

    public SubmissionAnalysisService(SubmissionRepository submissionRepository,
            FunctionalEvaluationRepository functionalEvaluationRepository,
            SecurityEvaluationRepository securityEvaluationRepository) {
        this.submissionRepository = submissionRepository;
        this.functionalEvaluationRepository = functionalEvaluationRepository;
        this.securityEvaluationRepository = securityEvaluationRepository;
    }

    @Transactional(readOnly = true)
    public SubmissionAnalysisResponse getForStudent(Long submissionId, String email) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));
        if (!submission.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot view this submission");
        }

        FunctionalEvaluation functional = functionalEvaluationRepository.findBySubmission_Id(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission evaluation not found"));
        Map<String, SecurityEvaluation> securityByEvaluator = securityEvaluationRepository.findBySubmission_Id(submissionId)
                .stream().collect(Collectors.toMap(item -> item.getEvaluatorName().toUpperCase(Locale.ROOT),
                        Function.identity(), (first, ignored) -> first));

        List<EvaluatorResultResponse> evaluators = new ArrayList<>();
        evaluators.add(new EvaluatorResultResponse("JUDGE0", "Judge0", functional.getStatus().name(), true, null));
        for (EvaluatorDefinition definition : EVALUATOR_CATALOG.subList(1, EVALUATOR_CATALOG.size())) {
            evaluators.add(toSecurityEvaluator(definition, securityByEvaluator.get(definition.id())));
        }

        EvidenceSummaryResponse evidence = evidenceSummary(securityByEvaluator.values());
        Assessment assessment = assessment(functional.getStatus(), evidence);
        return new SubmissionAnalysisResponse(submissionId, assessment.overallDecision(), evaluators, evidence,
                new AssessmentResponse(assessment.evidenceState()));
    }

    private EvaluatorResultResponse toSecurityEvaluator(EvaluatorDefinition definition, SecurityEvaluation evaluation) {
        if (evaluation == null) {
            return new EvaluatorResultResponse(definition.id(), definition.displayName(), "NOT_AVAILABLE", false, null);
        }
        String status = evaluation.getEvaluatorStatus();
        if ("COMPLETED".equals(status)) {
            return new EvaluatorResultResponse(definition.id(), definition.displayName(),
                    evaluation.isDetected() ? "VULNERABLE" : "SAFE", true, null);
        }
        if (status == null) { // Records saved before evaluator status was persisted.
            String legacyResult = "INFO".equals(evaluation.getSeverity()) ? "SAFE"
                    : evaluation.isDetected() ? "VULNERABLE" : "ERROR";
            return new EvaluatorResultResponse(definition.id(), definition.displayName(), legacyResult,
                    !"ERROR".equals(legacyResult), "ERROR".equals(legacyResult) ? "Legacy evaluator error" : null);
        }
        return new EvaluatorResultResponse(definition.id(), definition.displayName(), status, false,
                evaluation.getEvaluatorError());
    }

    private EvidenceSummaryResponse evidenceSummary(Iterable<SecurityEvaluation> evaluations) {
        int safe = 0;
        int vulnerable = 0;
        for (SecurityEvaluation evaluation : evaluations) {
            boolean completed = "COMPLETED".equals(evaluation.getEvaluatorStatus())
                    || (evaluation.getEvaluatorStatus() == null && !"EVALUATOR_ERROR".equals(evaluation.getSeverity()));
            if (!completed) continue;
            if (evaluation.isDetected()) vulnerable++;
            else safe++;
        }
        int total = safe + vulnerable;
        int safePercent = total == 0 ? 0 : Math.round(safe * 100.0f / total);
        return new EvidenceSummaryResponse(safePercent, total == 0 ? 0 : 100 - safePercent, safe, vulnerable);
    }

    private Assessment assessment(FunctionalEvaluationStatus functionalStatus, EvidenceSummaryResponse evidence) {
        if (functionalStatus != FunctionalEvaluationStatus.PASS) return new Assessment("NOT_AVAILABLE", "NOT_AVAILABLE");
        if (evidence.safeCount() + evidence.vulnerableCount() == 0) return new Assessment("NOT_AVAILABLE", "NO SECURITY EVIDENCE");
        if (evidence.vulnerableCount() == 0) return new Assessment("SECURITY PASS", "STRONG SAFE EVIDENCE");
        if (evidence.safeCount() == 0) return new Assessment("SECURITY FAIL", "STRONG VULNERABILITY EVIDENCE");
        if (evidence.safeCount() > evidence.vulnerableCount()) return new Assessment("SECURITY PASS", "MOSTLY SAFE — CONFLICT EXISTS");
        if (evidence.vulnerableCount() > evidence.safeCount()) return new Assessment("SECURITY FAIL", "MOSTLY VULNERABLE — CONFLICT EXISTS");
        return new Assessment("INCONCLUSIVE", "CONFLICTING EVIDENCE");
    }

    private record EvaluatorDefinition(String id, String displayName) { }
    private record Assessment(String overallDecision, String evidenceState) { }
}
