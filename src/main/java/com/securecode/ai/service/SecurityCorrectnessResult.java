package com.securecode.ai.service;

import com.securecode.ai.evaluation.SecurityCorrectnessStatus;
import java.util.Set;

/** The Step 2 score and its independent security gate. */
public record SecurityCorrectnessResult(Integer score, SecurityCorrectnessStatus status,
        Set<String> targetedFindingKeys) {
    public SecurityCorrectnessResult {
        targetedFindingKeys = Set.copyOf(targetedFindingKeys);
    }

    public boolean blocksMastery() {
        return status != SecurityCorrectnessStatus.VERIFIED_SAFE;
    }
}
