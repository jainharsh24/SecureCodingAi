package com.securecode.ai.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UnderstandingAssessmentRequest(@NotNull @Size(max = 4000) String answer) { }
