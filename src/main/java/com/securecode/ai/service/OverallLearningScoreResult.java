package com.securecode.ai.service;

/** Immutable Step 5 decision.  A provisional score is useful feedback, never a security claim. */
public record OverallLearningScoreResult(int displayedScore, Integer verifiedFinalScore, boolean provisional,
        String scoreState, boolean challengeCompleted, boolean challengeMastered) { }
