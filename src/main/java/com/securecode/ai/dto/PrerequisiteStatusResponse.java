package com.securecode.ai.dto;

/** Minimal student-safe prerequisite state; it intentionally contains no challenge source or tests. */
public record PrerequisiteStatusResponse(Long id, long challengeNumber, Integer catalogNumber, String title,
        String difficulty, boolean completed, boolean accessible) { }
