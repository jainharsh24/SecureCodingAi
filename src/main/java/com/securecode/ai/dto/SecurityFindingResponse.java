package com.securecode.ai.dto;

public record SecurityFindingResponse(String ruleId, String message, String severity, String vulnerabilityType,
        String cwe, Integer line, String code) { }
