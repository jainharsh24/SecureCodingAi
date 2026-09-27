package com.securecode.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.Difficulty;
import com.securecode.ai.entity.FunctionalEvaluation;
import com.securecode.ai.entity.FunctionalEvaluationStatus;
import com.securecode.ai.entity.Role;
import com.securecode.ai.entity.SecurityEvaluation;
import com.securecode.ai.entity.Submission;
import com.securecode.ai.entity.User;
import com.securecode.ai.repository.FunctionalEvaluationRepository;
import com.securecode.ai.repository.SecurityEvaluationRepository;
import com.securecode.ai.repository.SubmissionRepository;
import com.securecode.ai.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class StudentDataServiceTests {

    @Test
    void reportsConflictingEvidenceForAnEvenSafeVulnerableSplit() {
        UserRepository users = Mockito.mock(UserRepository.class);
        SubmissionRepository submissions = Mockito.mock(SubmissionRepository.class);
        FunctionalEvaluationRepository functionals = Mockito.mock(FunctionalEvaluationRepository.class);
        SecurityEvaluationRepository securities = Mockito.mock(SecurityEvaluationRepository.class);
        Submission submission = Mockito.mock(Submission.class);
        Challenge challenge = Mockito.mock(Challenge.class);
        when(users.findByEmail("student@example.test")).thenReturn(Optional.of(new User("student@example.test", "hash", Role.STUDENT)));
        when(submission.getId()).thenReturn(22L);
        when(submission.getChallenge()).thenReturn(challenge);
        when(challenge.getId()).thenReturn(4L);
        when(challenge.getTitle()).thenReturn("Conflict challenge");
        when(challenge.getDifficulty()).thenReturn(Difficulty.MEDIUM);
        when(submissions.findByUser_EmailOrderBySubmittedAtDesc("student@example.test")).thenReturn(List.of(submission));
        when(functionals.findBySubmission_Id(22L)).thenReturn(Optional.of(new FunctionalEvaluation(submission, FunctionalEvaluationStatus.PASS, 1, 1, null)));
        when(securities.findBySubmission_Id(22L)).thenReturn(List.of(
                new SecurityEvaluation(submission, "SEMGREP", false, null, null, "INFO", null, "{}", "COMPLETED", null),
                new SecurityEvaluation(submission, "SPOTBUGS", true, "Injection", "CWE-89", "HIGH", null, "{}", "COMPLETED", null)));

        var history = new StudentDataService(users, submissions, functionals, securities).history("student@example.test");

        assertThat(history).singleElement().satisfies(item -> {
            assertThat(item.safeEvidencePercent()).isEqualTo(50);
            assertThat(item.evidenceState()).isEqualTo("CONFLICTING EVIDENCE");
        });
    }
}
