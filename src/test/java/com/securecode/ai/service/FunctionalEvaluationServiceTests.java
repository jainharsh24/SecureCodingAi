package com.securecode.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.securecode.ai.dto.SubmissionRequest;
import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.Difficulty;
import com.securecode.ai.entity.Role;
import com.securecode.ai.entity.Submission;
import com.securecode.ai.entity.User;
import com.securecode.ai.evaluation.judge0.Judge0Client;
import com.securecode.ai.repository.ChallengeRepository;
import com.securecode.ai.repository.FunctionalEvaluationRepository;
import com.securecode.ai.repository.SubmissionRepository;
import com.securecode.ai.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FunctionalEvaluationServiceTests {
    @Test
    void unchangedStarterSkipsJudge0AndSecurityEvaluation() {
        ChallengeRepository challenges = mock(ChallengeRepository.class);
        UserRepository users = mock(UserRepository.class);
        SubmissionRepository submissions = mock(SubmissionRepository.class);
        FunctionalEvaluationRepository evaluations = mock(FunctionalEvaluationRepository.class);
        Judge0Client judge0 = mock(Judge0Client.class);
        SecurityEvaluationService security = mock(SecurityEvaluationService.class);
        PrerequisiteAccessService prerequisites = mock(PrerequisiteAccessService.class);
        Challenge challenge = new Challenge(1, "Example", "Description", "java", "Injection Attack", "SQL", "CWE-89",
                "class Main { // starter\n public static void main(String[] args) { } }", Difficulty.EASY);
        User user = new User("student@example.com", "unused", Role.STUDENT);
        when(challenges.findById(1L)).thenReturn(Optional.of(challenge));
        when(users.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(submissions.countByUser_IdAndChallenge_Id(any(), any())).thenReturn(0L);
        when(submissions.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doAnswer(invocation -> invocation.getArgument(0)).when(evaluations).save(any());

        FunctionalEvaluationService service = new FunctionalEvaluationService(challenges, users, submissions, evaluations,
                judge0, security, new JavaSourceEquivalence(), prerequisites);
        var response = service.submitAndEvaluate(1L, "student@example.com",
                new SubmissionRequest("class Main { public static void main(String[] args) { } }"));

        assertThat(response.status()).isEqualTo("STARTER_CODE");
        assertThat(response.learningScore()).isZero();
        verifyNoInteractions(judge0, security);
    }
}
