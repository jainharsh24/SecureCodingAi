package com.securecode.ai.evaluation.judge0;

/** The Judge0 outcome is kept separate from whether a challenge assertion passed. */
public record Judge0ExecutionResult(boolean completed, boolean accepted, String stdout, String details,
        Judge0ExecutionStatus executionStatus) {
    /** Retained for callers that construct a normal accepted/rejected result directly. */
    public Judge0ExecutionResult(boolean completed, boolean accepted, String stdout, String details) {
        this(completed, accepted, stdout, details,
                completed ? (accepted ? Judge0ExecutionStatus.ACCEPTED : Judge0ExecutionStatus.REJECTED)
                        : Judge0ExecutionStatus.UNVERIFIED);
    }

    public boolean compilationFailed() {
        return executionStatus == Judge0ExecutionStatus.COMPILATION_ERROR;
    }

    public boolean unverified() {
        return executionStatus == Judge0ExecutionStatus.UNVERIFIED;
    }
}
