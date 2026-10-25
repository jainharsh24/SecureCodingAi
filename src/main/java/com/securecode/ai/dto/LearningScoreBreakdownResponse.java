package com.securecode.ai.dto;

/** Persisted score snapshot for one submission; null values mean that component was not evaluated. */
public record LearningScoreBreakdownResponse(int overallScore, Integer verifiedFinalScore, String scoreState,
        Integer functionalScore, Integer securityScore, Integer understandingScore, Integer independentProblemSolvingScore,
        boolean challengeCompleted, boolean challengeMastered) { }
