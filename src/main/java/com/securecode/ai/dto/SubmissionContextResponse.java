package com.securecode.ai.dto;

/** Immutable challenge and source snapshot displayed alongside a saved submission analysis. */
public record SubmissionContextResponse(long challengeNumber, String challengeTitle, String challengeDescription,
        String language, String submittedCode) { }
