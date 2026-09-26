package com.securecode.ai.dto;

public record UserSummaryResponse(String name, String email, long challengesSolved, long challengesAttempted,
        long totalSubmissions, double averageLearningScore) { }
