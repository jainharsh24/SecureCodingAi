package com.securecode.ai.entity;

public enum HintAssistanceLevel {
    CONCEPTUAL(10),
    IMPLEMENTATION(5),
    SOLUTION(0);

    private final int independentProblemSolvingScore;
    HintAssistanceLevel(int independentProblemSolvingScore) { this.independentProblemSolvingScore = independentProblemSolvingScore; }
    public int independentProblemSolvingScore() { return independentProblemSolvingScore; }
}
