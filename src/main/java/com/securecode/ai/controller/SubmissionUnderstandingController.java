package com.securecode.ai.controller;

import com.securecode.ai.dto.UnderstandingAssessmentRequest;
import com.securecode.ai.dto.UnderstandingAssessmentResponse;
import com.securecode.ai.dto.UnderstandingQuestionResponse;
import com.securecode.ai.service.VulnerabilityUnderstandingService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Keeps answer attempts attached to the exact submission displayed on the analysis page. */
@RestController
@RequestMapping("/api/student/submissions/{submissionId}")
public class SubmissionUnderstandingController {
    private final VulnerabilityUnderstandingService service;
    public SubmissionUnderstandingController(VulnerabilityUnderstandingService service) { this.service = service; }
    @GetMapping("/understanding-question")
    public UnderstandingQuestionResponse question(@PathVariable Long submissionId, @RequestHeader("X-Assessment-Session") String token, Authentication auth) { return service.questionForSubmission(submissionId, auth.getName(), token); }
    @PostMapping("/understanding-assessments")
    public UnderstandingAssessmentResponse assess(@PathVariable Long submissionId, @RequestHeader("X-Assessment-Session") String token, @Valid @RequestBody UnderstandingAssessmentRequest request, Authentication auth) {
        return service.assessSubmission(submissionId, auth.getName(), token, request.answer());
    }
    @PostMapping("/understanding-session/close")
    public void close(@PathVariable Long submissionId, @RequestHeader("X-Assessment-Session") String token, Authentication auth) { service.closeSubmissionSession(submissionId, auth.getName(), token); }
}
