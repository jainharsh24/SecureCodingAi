package com.securecode.ai.dto;
public record FunctionalEvaluationResponse(Long submissionId, String status, int passedTests, int totalTests,
        SecurityEvaluationResponse securityEvaluation, int attemptNumber, int attemptsRemaining, int learningScore,
        Integer securityScore, String securityCorrectnessStatus, Integer independentProblemSolvingScore,
        String highestHintAssistance, Integer understandingScore, String understandingAssessmentStatus,
        Integer verifiedFinalScore, boolean provisional, String learningScoreState, boolean challengeCompleted,
        boolean challengeMastered, String understandingAssessmentSessionToken) { }
