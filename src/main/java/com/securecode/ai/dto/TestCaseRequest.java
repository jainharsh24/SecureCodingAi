package com.securecode.ai.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
public record TestCaseRequest(@NotNull String inputData, @NotNull String expectedOutput, boolean hidden, @PositiveOrZero int position) { }
