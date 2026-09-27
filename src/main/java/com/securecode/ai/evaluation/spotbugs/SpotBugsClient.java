package com.securecode.ai.evaluation.spotbugs;

public interface SpotBugsClient {
    SpotBugsScanResult scanJava(String sourceCode);
}
