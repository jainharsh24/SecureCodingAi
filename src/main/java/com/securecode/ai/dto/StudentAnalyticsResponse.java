package com.securecode.ai.dto;

import java.util.List;

public record StudentAnalyticsResponse(long totalQuestionsSolved, long totalPassedSubmissions,
        int highestSafeEvidencePercent, List<LearningProgressPoint> learningProgress,
        List<VulnerabilitySubtypeProgress> solvedByVulnerabilitySubtype, long totalActiveChallenges,
        long challengesAttempted, long challengesNotSolved, int completionPercent,
        List<DifficultyAccuracyResponse> accuracyByDifficulty, List<AnalyticsTrendPoint> weeklyTrends,
        List<AnalyticsTrendPoint> monthlyTrends, List<ChallengeProgressResponse> challengeProgress) {
    public StudentAnalyticsResponse {
        learningProgress = List.copyOf(learningProgress);
        solvedByVulnerabilitySubtype = List.copyOf(solvedByVulnerabilitySubtype);
        accuracyByDifficulty = List.copyOf(accuracyByDifficulty);
        weeklyTrends = List.copyOf(weeklyTrends);
        monthlyTrends = List.copyOf(monthlyTrends);
        challengeProgress = List.copyOf(challengeProgress);
    }
}
