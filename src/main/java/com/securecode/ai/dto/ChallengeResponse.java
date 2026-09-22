package com.securecode.ai.dto;
import java.util.List;
public record ChallengeResponse(Long id, String title, String description, String language, String vulnerabilityType,
        String vulnerabilitySubtype, String cwe, String starterCode, List<TestCaseResponse> testCases) { }
