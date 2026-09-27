package com.securecode.ai.evaluation.common;

/** The small, student-facing portion of a persisted evaluator finding. */
public record StoredSecurityFinding(String finding, String cwe) { }
