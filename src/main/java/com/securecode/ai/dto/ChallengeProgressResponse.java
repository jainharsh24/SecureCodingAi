package com.securecode.ai.dto;

public record ChallengeProgressResponse(long challengeId, int catalogNumber, String title, String difficulty,
        String vulnerabilitySubtype, String status, boolean locked, int completedPrerequisites,
        int prerequisiteCount, long attempts, String latestResult, int bestLearningScore) { }
