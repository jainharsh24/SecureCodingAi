package com.securecode.ai.controller;

import com.securecode.ai.dto.FunctionalEvaluationResponse;
import com.securecode.ai.dto.SubmissionRequest;
import com.securecode.ai.service.FunctionalEvaluationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/challenges/{challengeId}/submissions")
public class SubmissionController {
    private final FunctionalEvaluationService functionalEvaluationService;
    public SubmissionController(FunctionalEvaluationService functionalEvaluationService) {
        this.functionalEvaluationService = functionalEvaluationService;
    }
    @PostMapping
    public FunctionalEvaluationResponse submit(@PathVariable Long challengeId, @Valid @RequestBody SubmissionRequest request,
            Authentication authentication) {
        return functionalEvaluationService.submitAndEvaluate(challengeId, authentication.getName(), request);
    }
}
