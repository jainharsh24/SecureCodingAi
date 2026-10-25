package com.securecode.ai.dto;

public record UnderstandingAssessmentResponse(Long assessmentId, String status, int score, int rootCausePoints,
        int mechanismPoints, int preventionPoints, String rootCauseFeedback, String mechanismFeedback,
        String preventionFeedback, String unavailableReason) { }
