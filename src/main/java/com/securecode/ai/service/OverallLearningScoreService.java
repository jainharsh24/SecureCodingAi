package com.securecode.ai.service;

import com.securecode.ai.entity.FunctionalEvaluationStatus;
import com.securecode.ai.entity.UnderstandingAssessmentStatus;
import com.securecode.ai.evaluation.SecurityCorrectnessStatus;
import org.springframework.stereotype.Service;

/** Combines the four component scores without giving missing or conflicting security evidence a pass. */
@Service
public class OverallLearningScoreService {
    public static final int MASTERY_THRESHOLD = 80;

    public OverallLearningScoreResult calculate(FunctionalEvaluationStatus functionalStatus, int functionalScore,
            Integer securityScore, SecurityCorrectnessStatus securityStatus, Integer understandingScore,
            UnderstandingAssessmentStatus understandingStatus, Integer independentScore) {
        int available = bounded(functionalScore, 25) + bounded(securityScore, 45)
                + bounded(understandingScore, 15) + bounded(independentScore, 15);
        // A failed functional run can contribute the earned partial functional points to the
        // displayed progress score, but it must never make the aggregate a verified final score.
        boolean functionalVerified = functionalStatus == FunctionalEvaluationStatus.PASS;
        boolean securityVerifiedSafe = securityStatus == SecurityCorrectnessStatus.VERIFIED_SAFE;
        boolean allComponentsValid = functionalVerified && securityVerifiedSafe
                && understandingStatus == UnderstandingAssessmentStatus.ASSESSED && independentScore != null;
        boolean targetedVulnerability = securityStatus == SecurityCorrectnessStatus.TARGETED_VULNERABILITY;
        int capped = targetedVulnerability ? Math.min(49, available) : available;
        boolean completed = functionalStatus == FunctionalEvaluationStatus.PASS && securityVerifiedSafe;
        boolean mastered = completed && allComponentsValid && capped >= MASTERY_THRESHOLD
                && understandingScore != null && understandingScore == 15;
        String state = targetedVulnerability ? "TARGETED_VULNERABILITY"
                : securityStatus == SecurityCorrectnessStatus.CONFLICTING_EVIDENCE ? "CONFLICTING_EVIDENCE"
                : securityStatus == null || securityStatus == SecurityCorrectnessStatus.UNVERIFIED
                        || securityStatus == SecurityCorrectnessStatus.NOT_RUN ? "UNVERIFIED"
                : allComponentsValid ? "VERIFIED" : "PROVISIONAL";
        return new OverallLearningScoreResult(capped, allComponentsValid ? capped : null, !allComponentsValid,
                state, completed, mastered);
    }

    private int bounded(Integer value, int maximum) { return value == null ? 0 : Math.max(0, Math.min(maximum, value)); }
}
