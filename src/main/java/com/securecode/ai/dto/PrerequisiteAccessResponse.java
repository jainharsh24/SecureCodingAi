package com.securecode.ai.dto;

import java.util.List;

public record PrerequisiteAccessResponse(Long challengeId, boolean accessible,
        List<PrerequisiteStatusResponse> prerequisites) { }
