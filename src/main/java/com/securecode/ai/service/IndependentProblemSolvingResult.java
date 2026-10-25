package com.securecode.ai.service;

import com.securecode.ai.entity.HintAssistanceLevel;

public record IndependentProblemSolvingResult(int score, HintAssistanceLevel highestAssistance) { }
