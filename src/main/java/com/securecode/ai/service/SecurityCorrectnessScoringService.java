package com.securecode.ai.service;

import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.SecurityEvaluation;
import com.securecode.ai.evaluation.SecurityCorrectnessStatus;
import com.securecode.ai.evaluation.common.SecurityFindingEvidenceCodec;
import com.securecode.ai.evaluation.common.StoredSecurityFinding;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.LinkedHashSet;
import org.springframework.stereotype.Service;

/**
 * Scores only challenge-targeted security evidence.  This is deliberately a gate as
 * well as a score: no score from another component can turn an unverified or
 * conflicting result into a verified secure solution.
 */
@Service
public class SecurityCorrectnessScoringService {
    static final int MAX_SECURITY_SCORE = 45;
    private static final Set<String> REQUIRED_EVALUATORS = Set.of("SEMGREP", "SPOTBUGS", "GROQ");
    private final SecurityFindingEvidenceCodec evidenceCodec;

    public SecurityCorrectnessScoringService(SecurityFindingEvidenceCodec evidenceCodec) {
        this.evidenceCodec = evidenceCodec;
    }

    public SecurityCorrectnessResult score(Challenge challenge, List<SecurityEvaluation> evaluations) {
        if (!hasCompletedRequiredEvaluators(evaluations)) {
            return result(0, SecurityCorrectnessStatus.UNVERIFIED, Set.of());
        }

        Set<String> targetedFindings = new LinkedHashSet<>();
        boolean safeEvidence = false;
        boolean vulnerableEvidence = false;
        boolean uncertainEvidence = false;
        for (SecurityEvaluation evaluation : evaluations) {
            if (!REQUIRED_EVALUATORS.contains(upper(evaluation.getEvaluatorName()))) continue;
            Set<String> matches = targetedFindingKeys(challenge, evaluation);
            targetedFindings.addAll(matches); // A finding reported by several tools is one underlying issue.
            if (!matches.isEmpty()) {
                vulnerableEvidence = true;
                continue;
            }
            if ("GROQ".equalsIgnoreCase(evaluation.getEvaluatorName())) {
                if ("SAFE".equals(evaluation.getAssessment())) safeEvidence = true;
                else uncertainEvidence = true;
            } else {
                safeEvidence = true;
            }
        }

        if (uncertainEvidence || (safeEvidence && vulnerableEvidence)) {
            return result(0, SecurityCorrectnessStatus.CONFLICTING_EVIDENCE, targetedFindings);
        }
        if (vulnerableEvidence) {
            return result(0, SecurityCorrectnessStatus.TARGETED_VULNERABILITY, targetedFindings);
        }
        // All required evaluators completed and Groq explicitly assessed the target as SAFE.
        return result(MAX_SECURITY_SCORE, SecurityCorrectnessStatus.VERIFIED_SAFE, Set.of());
    }

    private boolean hasCompletedRequiredEvaluators(List<SecurityEvaluation> evaluations) {
        return REQUIRED_EVALUATORS.stream().allMatch(required -> evaluations.stream().anyMatch(evaluation ->
                required.equals(upper(evaluation.getEvaluatorName()))
                        && "COMPLETED".equals(evaluation.getEvaluatorStatus())))
                && evaluations.stream().filter(evaluation -> REQUIRED_EVALUATORS.contains(upper(evaluation.getEvaluatorName())))
                        .allMatch(evaluation -> "COMPLETED".equals(evaluation.getEvaluatorStatus()));
    }

    private Set<String> targetedFindingKeys(Challenge challenge, SecurityEvaluation evaluation) {
        List<StoredSecurityFinding> findings = evidenceCodec.decode(evaluation.getEvidence());
        if (findings.isEmpty() && evaluation.isDetected()) {
            findings = List.of(new StoredSecurityFinding(evaluation.getVulnerabilityType(), evaluation.getCwe()));
        }
        Set<String> matches = new LinkedHashSet<>();
        for (StoredSecurityFinding finding : findings) {
            if (targets(challenge, finding)) matches.add(key(finding));
        }
        return matches;
    }

    private boolean targets(Challenge challenge, StoredSecurityFinding finding) {
        String challengeCwe = normalized(challenge.getCwe());
        String findingCwe = normalized(finding.cwe());
        if (!challengeCwe.isBlank() && challengeCwe.equals(findingCwe)) return true;
        String findingLabel = normalized(finding.finding());
        return related(findingLabel, normalized(challenge.getVulnerabilityType()))
                || related(findingLabel, normalized(challenge.getVulnerabilitySubtype()));
    }

    private boolean related(String left, String right) {
        return !left.isBlank() && !right.isBlank() && (left.contains(right) || right.contains(left));
    }

    private String key(StoredSecurityFinding finding) {
        return normalized(finding.cwe()) + "|" + normalized(finding.finding());
    }

    private SecurityCorrectnessResult result(int score, SecurityCorrectnessStatus status, Set<String> findings) {
        return new SecurityCorrectnessResult(score, status, findings);
    }

    private String upper(String value) { return value == null ? "" : value.toUpperCase(Locale.ROOT); }
    private String normalized(String value) { return value == null ? "" : value.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT); }
}
