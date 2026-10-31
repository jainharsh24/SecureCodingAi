package com.securecode.ai.service;

import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.SecurityEvaluation;
import com.securecode.ai.evaluation.SecurityCorrectnessStatus;
import com.securecode.ai.evaluation.common.NormalizedEvaluatorEvidence;
import com.securecode.ai.evaluation.common.SecurityEvidenceSummary;
import com.securecode.ai.evaluation.common.SecurityFindingEvidenceCodec;
import com.securecode.ai.evaluation.common.StoredSecurityFinding;
import java.util.List;
import java.util.Locale;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Scores only challenge-targeted security evidence.  This is deliberately a gate as
 * well as a score: no score from another component can turn an unverified or
 * conflicting result into a verified secure solution.
 */
@Service
public class SecurityCorrectnessScoringService {
    static final int MAX_SECURITY_SCORE = 45;
    private final SecurityFindingEvidenceCodec evidenceCodec;

    public SecurityCorrectnessScoringService(SecurityFindingEvidenceCodec evidenceCodec) {
        this.evidenceCodec = evidenceCodec;
    }

    public SecurityCorrectnessResult score(Challenge challenge, List<SecurityEvaluation> evaluations) {
        SecurityEvidenceSummary evidence = SecurityEvidenceSummary.from(evaluations);
        if (evidence.validEvaluatorCount() == 0) {
            // An unavailable or malformed evaluator result is not a failed security check.
            return result(null, SecurityCorrectnessStatus.UNVERIFIED, Set.of());
        }

        Set<String> targetedFindings = new LinkedHashSet<>();
        for (SecurityEvaluation evaluation : evaluations) {
            var normalized = NormalizedEvaluatorEvidence.from(evaluation);
            if (normalized.isPresent() && normalized.get().verdict() == NormalizedEvaluatorEvidence.Verdict.VULNERABLE) {
                targetedFindings.addAll(targetedFindingKeys(challenge, evaluation));
            }
        }

        // Round exactly once, after dividing the fixed 45-point pool among valid verdicts.
        int score = Math.max(0, Math.min(MAX_SECURITY_SCORE,
                Math.round(evidence.safeCount() * (MAX_SECURITY_SCORE / (float) evidence.validEvaluatorCount()))));
        if (evidence.vulnerableCount() > 0) {
            // A confirmed vulnerability never becomes safe through averaging or partial credit.
            return result(score, SecurityCorrectnessStatus.TARGETED_VULNERABILITY, targetedFindings);
        }
        return result(score, SecurityCorrectnessStatus.VERIFIED_SAFE, Set.of());
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

    private SecurityCorrectnessResult result(Integer score, SecurityCorrectnessStatus status, Set<String> findings) {
        return new SecurityCorrectnessResult(score, status, findings);
    }

    private String normalized(String value) { return value == null ? "" : value.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT); }
}
