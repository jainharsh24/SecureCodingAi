package com.securecode.ai.evaluation;

public enum SecurityEvaluationStatus {
    COMPLETED,
    ERROR,
    TIMEOUT,
    /** The evaluator was deliberately turned off by configuration. */
    DISABLED,
    /** The evaluator was turned on but lacks mandatory configuration, such as an API key. */
    NOT_CONFIGURED,
    /** No evaluation was started (for example, functional evaluation did not pass). */
    NOT_RUN,
    /** @deprecated use the explicit DISABLED, NOT_CONFIGURED, or NOT_RUN state instead. */
    @Deprecated
    SKIPPED
}
