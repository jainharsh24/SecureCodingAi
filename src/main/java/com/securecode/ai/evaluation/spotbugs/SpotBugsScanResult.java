package com.securecode.ai.evaluation.spotbugs;

import com.securecode.ai.evaluation.SecurityEvaluationStatus;
import com.securecode.ai.evaluation.SecurityFinding;
import java.util.List;

public record SpotBugsScanResult(SecurityEvaluationStatus status, List<SecurityFinding> findings, String error) {
    public boolean detected() { return !findings.isEmpty(); }
}
