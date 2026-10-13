package com.securecode.ai.evaluation.semgrep.normalization;

/** Transforms one evaluator's native result into the common evidence model. */
public interface EvidenceNormalizer<T> {
    NormalizedSecurityResult normalize(T result);
}
