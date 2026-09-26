package com.securecode.ai.dto;

public record EvaluatorResultResponse(String evaluator, String displayName, String result, boolean available,
        String error) { }
