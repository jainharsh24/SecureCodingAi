package com.securecode.ai.dto;

/** PASS functional evaluations divided by PASS and FAIL functional evaluations. */
public record DifficultyAccuracyResponse(String difficulty, long successfulAttempts, long evaluatedAttempts,
        int accuracyPercent) { }
