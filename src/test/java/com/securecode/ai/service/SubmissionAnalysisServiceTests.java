package com.securecode.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.securecode.ai.dto.SubmissionAnalysisResponse;
import com.securecode.ai.entity.FunctionalEvaluation;
import com.securecode.ai.entity.FunctionalEvaluationStatus;
import com.securecode.ai.entity.Role;
import com.securecode.ai.entity.SecurityEvaluation;
import com.securecode.ai.entity.Submission;
import com.securecode.ai.entity.User;
import com.securecode.ai.repository.FunctionalEvaluationRepository;
import com.securecode.ai.repository.SecurityEvaluationRepository;
import com.securecode.ai.repository.SubmissionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SubmissionAnalysisServiceTests {

    @Test
    void returnsPersistedSemgrepEvidenceAndUnavailableFutureEvaluators() {
        SubmissionRepository submissions = Mockito.mock(SubmissionRepository.class);
        FunctionalEvaluationRepository functionalEvaluations = Mockito.mock(FunctionalEvaluationRepository.class);
        SecurityEvaluationRepository securityEvaluations = Mockito.mock(SecurityEvaluationRepository.class);
        Submission submission = Mockito.mock(Submission.class);
        User user = new User("student@example.com", "unused", Role.STUDENT);
        when(submission.getUser()).thenReturn(user);
        when(submissions.findById(7L)).thenReturn(Optional.of(submission));
        when(functionalEvaluations.findBySubmission_Id(7L))
                .thenReturn(Optional.of(new FunctionalEvaluation(submission, FunctionalEvaluationStatus.PASS, 2, 2, null)));
        when(securityEvaluations.findBySubmission_Id(7L)).thenReturn(List.of(new SecurityEvaluation(submission, "SEMGREP", false,
                null, null, "INFO", null, "{}", "COMPLETED", null)));

        SubmissionAnalysisResponse response = new SubmissionAnalysisService(submissions, functionalEvaluations,
                securityEvaluations).getForStudent(7L, "student@example.com");

        assertThat(response.overallDecision()).isEqualTo("SECURITY PASS");
        assertThat(response.assessment().evidenceState()).isEqualTo("STRONG SAFE EVIDENCE");
        assertThat(response.evidenceSummary().safeEvidencePercent()).isEqualTo(100);
        assertThat(response.evaluators()).extracting(item -> item.evaluator() + ":" + item.result())
                .containsExactly("JUDGE0:PASS", "SEMGREP:SAFE", "PMD:NOT_AVAILABLE", "SPOTBUGS:NOT_AVAILABLE", "LLM:NOT_AVAILABLE");
    }

    @Test
    void doesNotIssueSecurityVerdictWhenFunctionalEvaluationFails() {
        SubmissionRepository submissions = Mockito.mock(SubmissionRepository.class);
        FunctionalEvaluationRepository functionalEvaluations = Mockito.mock(FunctionalEvaluationRepository.class);
        SecurityEvaluationRepository securityEvaluations = Mockito.mock(SecurityEvaluationRepository.class);
        Submission submission = Mockito.mock(Submission.class);
        when(submission.getUser()).thenReturn(new User("student@example.com", "unused", Role.STUDENT));
        when(submissions.findById(8L)).thenReturn(Optional.of(submission));
        when(functionalEvaluations.findBySubmission_Id(8L))
                .thenReturn(Optional.of(new FunctionalEvaluation(submission, FunctionalEvaluationStatus.FAIL, 1, 2, null)));
        when(securityEvaluations.findBySubmission_Id(8L)).thenReturn(List.of());

        SubmissionAnalysisResponse response = new SubmissionAnalysisService(submissions, functionalEvaluations,
                securityEvaluations).getForStudent(8L, "student@example.com");

        assertThat(response.overallDecision()).isEqualTo("NOT_AVAILABLE");
        assertThat(response.assessment().evidenceState()).isEqualTo("NOT_AVAILABLE");
    }
}
