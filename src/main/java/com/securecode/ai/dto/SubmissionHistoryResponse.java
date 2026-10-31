package com.securecode.ai.dto;

import java.time.Instant;

public record SubmissionHistoryResponse(Long submissionId, Long challengeId, long challengeNumber, Integer catalogNumber, String challengeTitle, String difficulty,
        int attemptNumber, Instant submittedAt, String functionalStatus, String rawAssessment, int learningScore,
        int safeEvidencePercent, int vulnerabilityEvidencePercent, String evidenceState, boolean solved) { }
