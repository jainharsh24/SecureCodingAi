package com.securecode.ai.dto;

import java.util.List;

/** Persisted score snapshot for one submission; null values mean that component was not evaluated. */
public record LearningScoreBreakdownResponse(int overallScore, Integer verifiedFinalScore, String scoreState,
        Integer functionalScore, Integer securityScore, Integer understandingScore, Integer independentProblemSolvingScore,
        String understandingAssessmentStatus, String independenceAssessmentStatus, boolean functionallyCorrect,
        boolean securityVerified, String finalAssessment, boolean challengeCompleted, boolean challengeMastered,
        List<String> reasons) {
    public LearningScoreBreakdownResponse {
        reasons = List.copyOf(reasons);
    }
}
