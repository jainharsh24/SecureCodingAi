package com.securecode.ai.dto;

public record UnderstandingQuestionResponse(Long challengeId, String question, String rootCauseCriterion,
        String securityMechanismCriterion, String preventionCriterion) { }
