package com.securecode.ai.dto;

/** Learning guidance is deliberately separate from security findings and scoring. */
public record LearningFeedbackResponse(String state, String title, String summary, String guidance,
        String source, boolean available) { }
