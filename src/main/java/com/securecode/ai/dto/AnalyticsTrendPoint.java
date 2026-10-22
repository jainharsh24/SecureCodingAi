package com.securecode.ai.dto;

/** UTC ISO week or calendar month bucket; solved counts a challenge once in its first solved bucket. */
public record AnalyticsTrendPoint(String period, long evaluatedAttempts, long successfulAttempts,
        long challengesSolved, int accuracyPercent) { }
