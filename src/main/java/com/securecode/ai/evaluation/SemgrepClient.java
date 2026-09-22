package com.securecode.ai.evaluation;

public interface SemgrepClient {
    SemgrepScanResult scanJava(String sourceCode);
}
