package com.securecode.ai.evaluation;

/** The security gate for a challenge-targeted security assessment. */
public enum SecurityCorrectnessStatus {
    VERIFIED_SAFE,
    /** Static security evidence is safe, but not every configured evaluator completed. */
    AVAILABLE_EVIDENCE_SAFE,
    TARGETED_VULNERABILITY,
    CONFLICTING_EVIDENCE,
    UNVERIFIED,
    NOT_RUN
}
