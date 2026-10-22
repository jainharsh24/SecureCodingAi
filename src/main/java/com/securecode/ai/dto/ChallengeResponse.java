package com.securecode.ai.dto;
import java.util.List;
public record ChallengeResponse(Long id, long challengeNumber, Integer catalogNumber, String title, String description, String language, String vulnerabilityType,
        String vulnerabilitySubtype, String cwe, String starterCode, List<TestCaseResponse> testCases,
        String difficulty, int attemptsUsed, int attemptsRemaining, boolean solved, boolean locked,
        List<PrerequisiteStatusResponse> prerequisites) { }
