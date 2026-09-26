package com.securecode.ai.dto;

public record EvidenceSummaryResponse(int safeEvidencePercent, int vulnerabilityEvidencePercent, int safeCount,
        int vulnerableCount) { }
