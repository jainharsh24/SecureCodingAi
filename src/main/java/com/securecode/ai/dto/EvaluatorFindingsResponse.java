package com.securecode.ai.dto;

import java.util.List;

public record EvaluatorFindingsResponse(String evaluator, String displayName, List<SecurityFindingDisplayResponse> findings) {
    public EvaluatorFindingsResponse { findings = List.copyOf(findings); }
}
