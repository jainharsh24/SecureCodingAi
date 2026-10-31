package com.securecode.ai.service;

import com.securecode.ai.entity.FunctionalEvaluationStatus;
import com.securecode.ai.entity.UnderstandingAssessmentStatus;
import com.securecode.ai.evaluation.SecurityCorrectnessStatus;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/** Combines the four component scores without giving missing or conflicting security evidence a pass. */
@Service
public class OverallLearningScoreService {
    public static final int FUNCTIONAL_MAX = 25;
    public static final int SECURITY_MAX = 45;
    public static final int UNDERSTANDING_MAX = 15;
    public static final int INDEPENDENCE_MAX = 15;
    public static final int SOLVED_THRESHOLD = FUNCTIONAL_MAX + SECURITY_MAX;
    public static final int MASTERY_THRESHOLD = 90;
    public static final int MASTERY_COMPONENT_MINIMUM_EXCLUSIVE = 10;

    public OverallLearningScoreResult calculate(FunctionalEvaluationStatus functionalStatus, int functionalScore,
            Integer securityScore, SecurityCorrectnessStatus securityStatus, Integer understandingScore,
            UnderstandingAssessmentStatus understandingStatus, Integer independentScore) {
        int available = bounded(functionalScore, FUNCTIONAL_MAX) + bounded(securityScore, SECURITY_MAX)
                + bounded(understandingScore, UNDERSTANDING_MAX) + bounded(independentScore, INDEPENDENCE_MAX);
        // A failed functional run can contribute the earned partial functional points to the
        // displayed progress score, but it must never make the aggregate a verified final score.
        boolean functionalVerified = functionalStatus == FunctionalEvaluationStatus.PASS;
        boolean securityVerifiedSafe = securityStatus == SecurityCorrectnessStatus.VERIFIED_SAFE;
        boolean allComponentsValid = functionalVerified && securityVerifiedSafe;
        boolean targetedVulnerability = securityStatus == SecurityCorrectnessStatus.TARGETED_VULNERABILITY;
        int capped = targetedVulnerability ? Math.min(49, available) : available;
        // SOLVED is the verified functional and security baseline: 25 + 45 = 70.
        // Do not infer it from a status alone when reading malformed or legacy rows.
        boolean completed = functionalVerified && bounded(functionalScore, FUNCTIONAL_MAX) == FUNCTIONAL_MAX
                && securityVerifiedSafe && bounded(securityScore, SECURITY_MAX) == SECURITY_MAX
                && capped >= SOLVED_THRESHOLD;
        // Mastery is intentionally stricter than completion: the learner must independently
        // explain the concept perfectly and solve without recorded assistance.
        boolean mastered = completed && capped >= MASTERY_THRESHOLD
                && understandingStatus == UnderstandingAssessmentStatus.ASSESSED
                && understandingScore != null && understandingScore > MASTERY_COMPONENT_MINIMUM_EXCLUSIVE
                && independentScore != null && independentScore > MASTERY_COMPONENT_MINIMUM_EXCLUSIVE;
        String state = targetedVulnerability ? "TARGETED_VULNERABILITY"
                : securityStatus == SecurityCorrectnessStatus.CONFLICTING_EVIDENCE ? "CONFLICTING_EVIDENCE"
                : securityStatus == null || securityStatus == SecurityCorrectnessStatus.UNVERIFIED
                        || securityStatus == SecurityCorrectnessStatus.NOT_RUN ? "UNVERIFIED"
                : allComponentsValid ? "VERIFIED" : "PROVISIONAL";
        return new OverallLearningScoreResult(capped, allComponentsValid ? capped : null, !allComponentsValid,
                state, completed, mastered, functionalVerified, securityVerifiedSafe,
                finalAssessment(functionalVerified, securityVerifiedSafe, completed, mastered),
                reasons(functionalStatus, securityStatus, understandingStatus, understandingScore, independentScore, mastered));
    }

    private String finalAssessment(boolean functionallyCorrect, boolean securityVerified, boolean completed, boolean mastered) {
        if (mastered) return "MASTERED";
        if (completed) return "COMPLETED";
        if (!functionallyCorrect) return "FUNCTIONAL_REQUIREMENTS_NOT_MET";
        if (!securityVerified) return "SECURITY_NOT_VERIFIED";
        return "INCOMPLETE";
    }

    private List<String> reasons(FunctionalEvaluationStatus functionalStatus, SecurityCorrectnessStatus securityStatus,
            UnderstandingAssessmentStatus understandingStatus, Integer understandingScore, Integer independentScore,
            boolean mastered) {
        List<String> reasons = new ArrayList<>();
        if (functionalStatus != FunctionalEvaluationStatus.PASS) reasons.add("Functional evaluation did not pass.");
        if (securityStatus == SecurityCorrectnessStatus.TARGETED_VULNERABILITY) {
            reasons.add("A challenge-targeted vulnerability was confirmed.");
        } else if (securityStatus == SecurityCorrectnessStatus.CONFLICTING_EVIDENCE) {
            reasons.add("Security evaluators reported conflicting evidence.");
        } else if (securityStatus != SecurityCorrectnessStatus.VERIFIED_SAFE) {
            reasons.add("Required security evidence is unavailable or unverified.");
        }
        if (understandingStatus != UnderstandingAssessmentStatus.ASSESSED) {
            reasons.add("Vulnerability understanding has not been assessed.");
        } else if (understandingScore == null || understandingScore <= MASTERY_COMPONENT_MINIMUM_EXCLUSIVE) {
            reasons.add("Understanding assessment has not met the mastery criterion.");
        }
        if (independentScore == null) reasons.add("Independence has not been assessed.");
        else if (independentScore <= MASTERY_COMPONENT_MINIMUM_EXCLUSIVE && !mastered) {
            reasons.add("Recorded assistance prevents independent mastery.");
        }
        if (reasons.isEmpty()) reasons.add("All mastery criteria were satisfied.");
        return reasons;
    }

    private int bounded(Integer value, int maximum) { return value == null ? 0 : Math.max(0, Math.min(maximum, value)); }
}
