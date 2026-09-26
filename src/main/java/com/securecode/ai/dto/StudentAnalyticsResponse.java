package com.securecode.ai.dto;

import java.util.List;

public record StudentAnalyticsResponse(long totalQuestionsSolved, long totalPassedSubmissions,
        int highestSafeEvidencePercent, List<LearningProgressPoint> learningProgress,
        List<VulnerabilitySubtypeProgress> solvedByVulnerabilitySubtype) {
    public StudentAnalyticsResponse {
        learningProgress = List.copyOf(learningProgress);
        solvedByVulnerabilitySubtype = List.copyOf(solvedByVulnerabilitySubtype);
    }
}
