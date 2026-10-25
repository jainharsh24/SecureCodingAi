package com.securecode.ai.evaluation.groq;

/** Structured, backend-validated rubric result; each criterion is either 0 or 5. */
public record GroqUnderstandingEvaluation(int rootCausePoints, int mechanismPoints, int preventionPoints,
        String rootCauseFeedback, String mechanismFeedback, String preventionFeedback) { }
