package com.securecode.ai.evaluation;

/** The security gate for a challenge-targeted security assessment. */
public enum SecurityCorrectnessStatus {
    VERIFIED_SAFE,
    TARGETED_VULNERABILITY,
    CONFLICTING_EVIDENCE,
    UNVERIFIED,
    NOT_RUN
}
