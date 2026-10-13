package com.securecode.ai.evaluation.semgrep;

public interface SemgrepClient {
    SemgrepScanResult scanJava(String sourceCode);
}
