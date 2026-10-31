package com.securecode.ai.service;

import com.securecode.ai.dto.AssessmentResponse;
import com.securecode.ai.dto.EvaluatorResultResponse;
import com.securecode.ai.dto.EvaluatorFindingsResponse;
import com.securecode.ai.dto.EvidenceSummaryResponse;
import com.securecode.ai.dto.SecurityFindingDisplayResponse;
import com.securecode.ai.dto.SubmissionAnalysisResponse;
import com.securecode.ai.dto.LearningFeedbackResponse;
import com.securecode.ai.dto.SubmissionContextResponse;
import com.securecode.ai.dto.LearningScoreBreakdownResponse;
import com.securecode.ai.evaluation.common.SecurityFindingEvidenceCodec;
import com.securecode.ai.evaluation.common.SecurityEvidenceSummary;
import com.securecode.ai.evaluation.common.StoredSecurityFinding;
import com.securecode.ai.entity.FunctionalEvaluation;
import com.securecode.ai.entity.FunctionalEvaluationStatus;
import com.securecode.ai.entity.SecurityEvaluation;
import com.securecode.ai.entity.Submission;
import com.securecode.ai.entity.Challenge;
import com.securecode.ai.repository.ChallengeRepository;
import com.securecode.ai.repository.FunctionalEvaluationRepository;
import com.securecode.ai.repository.SecurityEvaluationRepository;
import com.securecode.ai.repository.SubmissionRepository;
import com.securecode.ai.repository.UnderstandingAssessmentRepository;
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
            new EvaluatorDefinition("SPOTBUGS", "SpotBugs"),
            new EvaluatorDefinition("GROQ", "LLM"));

    private final SubmissionRepository submissionRepository;
    private final FunctionalEvaluationRepository functionalEvaluationRepository;
    private final SecurityEvaluationRepository securityEvaluationRepository;
    private final SecurityFindingEvidenceCodec findingEvidenceCodec;
    private final ChallengeRepository challenges;
    private final UnderstandingAssessmentRepository understandingAssessments;

    public SubmissionAnalysisService(SubmissionRepository submissionRepository,
            FunctionalEvaluationRepository functionalEvaluationRepository,
            SecurityEvaluationRepository securityEvaluationRepository, SecurityFindingEvidenceCodec findingEvidenceCodec) {
        this(submissionRepository, functionalEvaluationRepository, securityEvaluationRepository, findingEvidenceCodec, null, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public SubmissionAnalysisService(SubmissionRepository submissionRepository,
            FunctionalEvaluationRepository functionalEvaluationRepository,
            SecurityEvaluationRepository securityEvaluationRepository, SecurityFindingEvidenceCodec findingEvidenceCodec,
            ChallengeRepository challenges, UnderstandingAssessmentRepository understandingAssessments) {
        this.submissionRepository = submissionRepository;
        this.functionalEvaluationRepository = functionalEvaluationRepository;
        this.securityEvaluationRepository = securityEvaluationRepository;
        this.findingEvidenceCodec = findingEvidenceCodec;
        this.challenges = challenges;
        this.understandingAssessments = understandingAssessments;
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
        Assessment assessment = assessment(functional.getStatus(), submission.getSecurityCorrectnessStatus(), evidence, securityByEvaluator);
        var score = scoreBreakdown(submission, functional);
        String explanation = understandingAssessments == null ? null : understandingAssessments
                .findFirstByUser_IdAndSubmission_IdOrderByIdDesc(submission.getUser().getId(), submissionId)
                .map(com.securecode.ai.entity.UnderstandingAssessment::getAnswer)
                .filter(answer -> !answer.isBlank()).orElse(null);
        return new SubmissionAnalysisResponse(submissionId, assessment.overallDecision(), evaluators, evidence,
                new AssessmentResponse(assessment.evidenceState()), findingGroups(securityByEvaluator),
                learningFeedback(functional.getStatus(), evidence, securityByEvaluator.get("GROQ")),
                new SubmissionContextResponse(submission.getChallenge().getChallengeNumber(), catalogNumber(submission.getChallenge()), submission.getChallenge().getTitle(),
                        submission.getChallenge().getDescription(), submission.getLanguage(), submission.getSourceCode()),
                score, explanation);
    }

    private LearningFeedbackResponse learningFeedback(FunctionalEvaluationStatus functionalStatus,
            EvidenceSummaryResponse evidence, SecurityEvaluation groq) {
        if (functionalStatus == FunctionalEvaluationStatus.FAIL) return null;
        if (groq == null || !"COMPLETED".equals(groq.getEvaluatorStatus()) || groq.getAssessment() == null
                || groq.getFeedbackSummary() == null || groq.getFeedbackSummary().isBlank()
                || groq.getRemediation() == null || groq.getRemediation().isBlank()) {
            String detail = groq == null ? "The LLM learning evaluator was not run for this submission."
                    : groq.getEvaluatorError() == null || groq.getEvaluatorError().isBlank()
                    ? "The LLM learning evaluator did not return usable feedback."
                    : "The LLM learning evaluator was unavailable: " + groq.getEvaluatorError();
            return new LearningFeedbackResponse("UNAVAILABLE", "Learning feedback unavailable", detail,
                    "Use the available evaluator results and security findings to guide your next revision.", "LLM", false);
        }
        String state = groq.getAssessment();
        if (evidence.safeCount() > 0 && evidence.vulnerableCount() > 0) {
            return new LearningFeedbackResponse("UNCERTAIN", "Cautious next step",
                    "Available security evaluators disagree. The LLM review says: " + groq.getFeedbackSummary(),
                    groq.getRemediation(), "LLM", true);
        }
        return switch (state) {
            case "SAFE" -> new LearningFeedbackResponse("SAFE", "What you did well", groq.getFeedbackSummary(),
                    groq.getRemediation(), "LLM", true);
            case "VULNERABLE" -> new LearningFeedbackResponse("VULNERABLE", "Security improvement needed",
                    groq.getFeedbackSummary(),
                    groq.getRemediation(), "LLM", true);
            default -> new LearningFeedbackResponse("UNCERTAIN", "Cautious next step",
                    groq.getFeedbackSummary(), groq.getRemediation(), "LLM", true);
        };
    }

    private List<EvaluatorFindingsResponse> findingGroups(Map<String, SecurityEvaluation> securityByEvaluator) {
        return EVALUATOR_CATALOG.subList(1, EVALUATOR_CATALOG.size()).stream()
                .map(definition -> findingGroup(definition, securityByEvaluator.get(definition.id())))
                .filter(java.util.Objects::nonNull).toList();
    }

    private EvaluatorFindingsResponse findingGroup(EvaluatorDefinition definition, SecurityEvaluation evaluation) {
        if (evaluation == null || !evaluation.isDetected()) return null;
        List<StoredSecurityFinding> stored = findingEvidenceCodec.decode(evaluation.getEvidence());
        if (stored.isEmpty() && evaluation.getVulnerabilityType() != null) {
            stored = List.of(new StoredSecurityFinding(evaluation.getVulnerabilityType(), evaluation.getCwe()));
        }
        List<SecurityFindingDisplayResponse> findings = stored.stream()
                .map(finding -> new SecurityFindingDisplayResponse(finding.finding(), finding.cwe())).distinct().toList();
        return findings.isEmpty() ? null : new EvaluatorFindingsResponse(definition.id(), definition.displayName(), findings);
    }

    private EvaluatorResultResponse toSecurityEvaluator(EvaluatorDefinition definition, SecurityEvaluation evaluation) {
        if (evaluation == null) {
            if ("GROQ".equals(definition.id())) {
                return new EvaluatorResultResponse(definition.id(), definition.displayName(), "NOT_RUN", false,
                        "LLM evaluation was not run because this submission did not reach security evaluation");
            }
            return new EvaluatorResultResponse(definition.id(), definition.displayName(), "NOT_AVAILABLE", false, null);
        }
        String status = evaluation.getEvaluatorStatus();
        if ("COMPLETED".equals(status)) {
            if ("GROQ".equals(definition.id())) {
                if ("SAFE".equals(evaluation.getAssessment()) || "VULNERABLE".equals(evaluation.getAssessment())) {
                    return new EvaluatorResultResponse(definition.id(), definition.displayName(), evaluation.getAssessment(), true, null);
                }
                if ("UNCERTAIN".equals(evaluation.getAssessment())) {
                    return new EvaluatorResultResponse(definition.id(), definition.displayName(), "UNCERTAIN", true,
                            "The LLM could not reach a confident conclusion");
                }
                return new EvaluatorResultResponse(definition.id(), definition.displayName(), "UNVERIFIED", false,
                        evaluation.getEvaluatorError() == null ? "The LLM response did not contain a valid security verdict"
                                : evaluation.getEvaluatorError());
            }
            return new EvaluatorResultResponse(definition.id(), definition.displayName(),
                    evaluation.isDetected() ? "VULNERABLE" : "SAFE", true, null);
        }
        if (status == null) { // Records saved before evaluator status was persisted.
            // A status-less record cannot prove a successful scan.  In particular, older
            // failures could have an INFO severity and were incorrectly rendered as SAFE.
            String legacyResult = evaluation.isDetected() ? "VULNERABLE" : "UNVERIFIED";
            return new EvaluatorResultResponse(definition.id(), definition.displayName(), legacyResult,
                    evaluation.isDetected(), evaluation.isDetected() ? null : "Legacy evaluator outcome was not verified");
        }
        return new EvaluatorResultResponse(definition.id(), definition.displayName(), status, false,
                evaluation.getEvaluatorError());
    }

    private EvidenceSummaryResponse evidenceSummary(Iterable<SecurityEvaluation> evaluations) {
        SecurityEvidenceSummary summary = SecurityEvidenceSummary.from(evaluations);
        return new EvidenceSummaryResponse(summary.safePercent(), summary.vulnerabilityPercent(), summary.safeCount(),
                summary.vulnerableCount(), summary.validEvaluatorCount());
    }

    private Assessment assessment(FunctionalEvaluationStatus functionalStatus, String securityStatus,
            EvidenceSummaryResponse evidence, Map<String, SecurityEvaluation> securityByEvaluator) {
        if (functionalStatus == FunctionalEvaluationStatus.FAIL) return new Assessment("Functional Fail", "Functional Fail");
        if (functionalStatus == FunctionalEvaluationStatus.STARTER_CODE) return new Assessment("Submitted code is starter code", "INCOMPLETE ATTEMPT");
        if (functionalStatus != FunctionalEvaluationStatus.PASS) return new Assessment("NOT_AVAILABLE", "NOT_AVAILABLE");
        if (evidence.validEvaluatorCount() == 0) return new Assessment("NOT_AVAILABLE", "UNVERIFIED");
        if ("TARGETED_VULNERABILITY".equals(securityStatus)) return new Assessment("SECURITY FAIL", "TARGETED VULNERABILITY");
        if ("CONFLICTING_EVIDENCE".equals(securityStatus)) return new Assessment("INCONCLUSIVE", "CONFLICTING EVIDENCE");
        if ("UNVERIFIED".equals(securityStatus) || "NOT_RUN".equals(securityStatus)) {
            // Older rows may have been persisted before partial-evidence decisions were
            // introduced. Their read model can still report the available static evidence
            // without altering the historical score or verification state.
            boolean staticEvidence = List.of("SEMGREP", "SPOTBUGS").stream().map(securityByEvaluator::get)
                    .anyMatch(item -> item != null && "COMPLETED".equals(item.getEvaluatorStatus()));
            if (staticEvidence && evidence.vulnerableCount() > 0 && evidence.safeCount() == 0) {
                return new Assessment("SECURITY FAIL", "VULNERABILITY FROM AVAILABLE EVIDENCE");
            }
            return new Assessment("NOT_AVAILABLE", "UNVERIFIED");
        }
        if ("AVAILABLE_EVIDENCE_SAFE".equals(securityStatus)) {
            return new Assessment("SECURITY PASS", "SAFE FROM AVAILABLE EVIDENCE");
        }
        if ("VERIFIED_SAFE".equals(securityStatus)) return new Assessment("SECURITY PASS", "VERIFIED SAFE");
        if (evidence.vulnerableCount() == 0) return new Assessment("SECURITY PASS", "STRONG SAFE EVIDENCE");
        if (evidence.safeCount() == 0) return new Assessment("SECURITY FAIL", "STRONG VULNERABILITY EVIDENCE");
        if (evidence.safeCount() > evidence.vulnerableCount()) return new Assessment("SECURITY PASS", "MOSTLY SAFE — CONFLICT EXISTS");
        if (evidence.vulnerableCount() > evidence.safeCount()) return new Assessment("SECURITY FAIL", "MOSTLY VULNERABLE — CONFLICT EXISTS");
        return new Assessment("INCONCLUSIVE", "CONFLICTING EVIDENCE");
    }

    private Integer functionalScore(FunctionalEvaluation evaluation) {
        if ((evaluation.getStatus() != FunctionalEvaluationStatus.PASS && evaluation.getStatus() != FunctionalEvaluationStatus.FAIL)
                || evaluation.getTotalTests() == 0) return 0;
        return Math.round(25f * evaluation.getPassedTests() / evaluation.getTotalTests());
    }

    private LearningScoreBreakdownResponse scoreBreakdown(Submission submission, FunctionalEvaluation functional) {
        boolean functionallyCorrect = functional.getStatus() == FunctionalEvaluationStatus.PASS;
        boolean securityVerified = "VERIFIED_SAFE".equals(submission.getSecurityCorrectnessStatus());
        String understandingStatus = submission.getUnderstandingAssessmentStatus() == null ? "UNASSESSED"
                : submission.getUnderstandingAssessmentStatus();
        String independenceStatus = submission.getIndependentProblemSolvingScore() == null ? "UNASSESSED" : "ASSESSED";
        String finalAssessment = submission.isVerifiedMastered() ? "MASTERED"
                : submission.isVerifiedCompleted() ? "COMPLETED"
                : !functionallyCorrect ? "FUNCTIONAL_REQUIREMENTS_NOT_MET"
                : !securityVerified ? "SECURITY_NOT_VERIFIED" : "INCOMPLETE";
        return new LearningScoreBreakdownResponse(submission.getLearningScore(), submission.getVerifiedFinalScore(),
                submission.getLearningScoreState(), functionalScore(functional), submission.getSecurityScore(),
                submission.getUnderstandingScore(), submission.getIndependentProblemSolvingScore(), understandingStatus,
                independenceStatus, functionallyCorrect, securityVerified, finalAssessment,
                submission.isVerifiedCompleted(), submission.isVerifiedMastered(), scoreReasons(submission, functionallyCorrect, securityVerified));
    }

    private List<String> scoreReasons(Submission submission, boolean functionallyCorrect, boolean securityVerified) {
        List<String> reasons = new ArrayList<>();
        if (!functionallyCorrect) reasons.add("Functional evaluation did not pass.");
        String security = submission.getSecurityCorrectnessStatus();
        if ("TARGETED_VULNERABILITY".equals(security)) reasons.add("A challenge-targeted vulnerability was confirmed.");
        else if ("CONFLICTING_EVIDENCE".equals(security)) reasons.add("Security evaluators reported conflicting evidence.");
        else if (!securityVerified) reasons.add("Required security evidence is unavailable or unverified.");
        if (!"ASSESSED".equals(submission.getUnderstandingAssessmentStatus())) reasons.add("Vulnerability understanding has not been assessed.");
        else if (submission.getUnderstandingScore() == null
                || submission.getUnderstandingScore() <= OverallLearningScoreService.MASTERY_COMPONENT_MINIMUM_EXCLUSIVE) {
            reasons.add("Understanding assessment has not met the mastery criterion.");
        }
        if (submission.getIndependentProblemSolvingScore() == null) reasons.add("Independence has not been assessed.");
        else if (!submission.isVerifiedMastered() && submission.getIndependentProblemSolvingScore() <= OverallLearningScoreService.MASTERY_COMPONENT_MINIMUM_EXCLUSIVE) {
            reasons.add("Recorded assistance prevents independent mastery.");
        }
        if (reasons.isEmpty()) reasons.add("All mastery criteria were satisfied.");
        return reasons;
    }

    private Integer catalogNumber(Challenge challenge) {
        if (challenges == null || !challenge.isActive()) return null;
        List<Challenge> active = challenges.findByActiveTrueOrderByChallengeNumberAsc();
        for (int index = 0; index < active.size(); index++) {
            if (active.get(index).getId().equals(challenge.getId())) return index + 1;
        }
        return null;
    }

    private record EvaluatorDefinition(String id, String displayName) { }
    private record Assessment(String overallDecision, String evidenceState) { }
}
