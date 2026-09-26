package com.securecode.ai.controller;

import com.securecode.ai.dto.SubmissionAnalysisResponse;
import com.securecode.ai.service.SubmissionAnalysisService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/submissions")
public class SubmissionAnalysisController {
    private final SubmissionAnalysisService submissionAnalysisService;

    public SubmissionAnalysisController(SubmissionAnalysisService submissionAnalysisService) {
        this.submissionAnalysisService = submissionAnalysisService;
    }

    @GetMapping("/{submissionId}/analysis")
    public SubmissionAnalysisResponse getAnalysis(@PathVariable Long submissionId, Authentication authentication) {
        return submissionAnalysisService.getForStudent(submissionId, authentication.getName());
    }
}
