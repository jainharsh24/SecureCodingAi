package com.securecode.ai.dto;
public record TestCaseResponse(Long id, String inputData, String expectedOutput, boolean hidden, int position) { }
