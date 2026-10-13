package com.securecode.ai.evaluation.judge0;

public interface Judge0Client {
    Judge0ExecutionResult execute(String language, String sourceCode, String standardInput);
}
