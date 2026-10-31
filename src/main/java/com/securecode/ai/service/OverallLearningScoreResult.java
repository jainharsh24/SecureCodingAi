package com.securecode.ai.service;

import java.util.List;

/** Immutable Step 5 decision.  A provisional score is useful feedback, never a security claim. */
public record OverallLearningScoreResult(int displayedScore, Integer verifiedFinalScore, boolean provisional,
        String scoreState, boolean challengeCompleted, boolean challengeMastered, boolean functionallyCorrect,
        boolean securityVerified, String finalAssessment, List<String> reasons) {
    public OverallLearningScoreResult {
        reasons = List.copyOf(reasons);
    }
}
