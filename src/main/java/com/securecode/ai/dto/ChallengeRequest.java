package com.securecode.ai.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
public record ChallengeRequest(@NotBlank @Size(max = 255) String title, @NotBlank String description,
        @NotBlank @Size(max = 32) String language, @Size(max = 100) String vulnerabilityType,
        @Size(max = 100) String vulnerabilitySubtype, @Size(max = 32) String cwe, String starterCode,
        @Valid List<TestCaseRequest> testCases) {
    public ChallengeRequest { testCases = testCases == null ? List.of() : List.copyOf(testCases); }
}
