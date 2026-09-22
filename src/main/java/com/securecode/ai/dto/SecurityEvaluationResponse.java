package com.securecode.ai.dto;

import com.securecode.ai.evaluation.normalization.NormalizedSecurityResult;
import java.util.List;

public record SecurityEvaluationResponse(String evaluator, String status, boolean detected,
        List<SecurityFindingResponse> findings, String error, NormalizedSecurityResult normalizedResult) { }
