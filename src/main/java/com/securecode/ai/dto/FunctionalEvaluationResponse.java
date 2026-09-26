package com.securecode.ai.dto;
public record FunctionalEvaluationResponse(Long submissionId, String status, int passedTests, int totalTests,
        SecurityEvaluationResponse securityEvaluation, int attemptNumber, int attemptsRemaining, int learningScore) { }
