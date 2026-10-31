package com.securecode.ai.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import com.securecode.ai.entity.Difficulty;
public record ChallengeRequest(@NotBlank @Size(max = 255) String title, @NotBlank String description,
        @NotBlank @Size(max = 32) String language, @Size(max = 100) String vulnerabilityType,
        @Size(max = 100) String vulnerabilitySubtype, @Size(max = 32) String cwe, String starterCode,
        @Valid List<TestCaseRequest> testCases, Difficulty difficulty,
        List<Long> prerequisiteChallengeNumbers) {
    public ChallengeRequest {
        testCases = testCases == null ? List.of() : List.copyOf(testCases);
        difficulty = difficulty == null ? Difficulty.MEDIUM : difficulty;
        prerequisiteChallengeNumbers = prerequisiteChallengeNumbers == null ? List.of() : List.copyOf(prerequisiteChallengeNumbers);
    }
    public ChallengeRequest(String title, String description, String language, String vulnerabilityType, String vulnerabilitySubtype,
            String cwe, String starterCode, List<TestCaseRequest> testCases) {
        this(title, description, language, vulnerabilityType, vulnerabilitySubtype, cwe, starterCode, testCases, Difficulty.MEDIUM, List.of());
    }
    public ChallengeRequest(String title, String description, String language, String vulnerabilityType, String vulnerabilitySubtype,
            String cwe, String starterCode, List<TestCaseRequest> testCases, Difficulty difficulty) {
        this(title, description, language, vulnerabilityType, vulnerabilitySubtype, cwe, starterCode, testCases, difficulty, List.of());
    }
}
