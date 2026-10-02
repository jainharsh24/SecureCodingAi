package com.securecode.ai.dto;

import java.time.Instant;

public record LearningProgressPoint(long challengeNumber, String challengeTitle, int attemptNumber, int learningScore, Instant submittedAt) { }
