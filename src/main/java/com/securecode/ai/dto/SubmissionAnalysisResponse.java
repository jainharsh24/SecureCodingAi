package com.securecode.ai.dto;

import java.util.List;

public record SubmissionAnalysisResponse(Long submissionId, String overallDecision, List<EvaluatorResultResponse> evaluators,
        EvidenceSummaryResponse evidenceSummary, AssessmentResponse assessment) {
    public SubmissionAnalysisResponse {
        evaluators = List.copyOf(evaluators);
    }
}
