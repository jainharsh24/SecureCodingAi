package com.securecode.ai.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.securecode.ai.dto.SubmissionRequest;
import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.Difficulty;
import com.securecode.ai.entity.Role;
import com.securecode.ai.entity.Submission;
import com.securecode.ai.entity.TestCase;
import com.securecode.ai.entity.User;
import com.securecode.ai.evaluation.judge0.Judge0Client;
import com.securecode.ai.evaluation.judge0.Judge0ExecutionResult;
import com.securecode.ai.evaluation.judge0.Judge0ExecutionStatus;
import com.securecode.ai.repository.ChallengeRepository;
import com.securecode.ai.repository.FunctionalEvaluationRepository;
import com.securecode.ai.repository.SubmissionRepository;
import com.securecode.ai.repository.UserRepository;
import com.securecode.ai.repository.UnderstandingAssessmentRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FunctionalEvaluationServiceTests {
    @Test
    void unchangedStarterIsRejectedBeforeItConsumesAnAttempt() {
        ChallengeRepository challenges = mock(ChallengeRepository.class);
        UserRepository users = mock(UserRepository.class);
        SubmissionRepository submissions = mock(SubmissionRepository.class);
        FunctionalEvaluationRepository evaluations = mock(FunctionalEvaluationRepository.class);
        Judge0Client judge0 = mock(Judge0Client.class);
        SecurityEvaluationService security = mock(SecurityEvaluationService.class);
        PrerequisiteAccessService prerequisites = mock(PrerequisiteAccessService.class);
        AttemptWindowService attemptWindows = mock(AttemptWindowService.class);
        Challenge challenge = new Challenge(1, "Example", "Description", "java", "Injection Attack", "SQL", "CWE-89",
                "class Main { // starter\n public static void main(String[] args) { } }", Difficulty.EASY);
        User user = new User("student@example.com", "unused", Role.STUDENT);
        when(challenges.findById(1L)).thenReturn(Optional.of(challenge));
        when(users.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(submissions.countByUser_IdAndChallenge_Id(any(), any())).thenReturn(0L);
        when(submissions.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doAnswer(invocation -> invocation.getArgument(0)).when(evaluations).save(any());

        FunctionalEvaluationService service = new FunctionalEvaluationService(challenges, users, submissions, evaluations,
                judge0, security, new JavaSourceEquivalence(), prerequisites, attemptWindows,
                mock(SecurityCorrectnessScoringService.class), mock(IndependentProblemSolvingService.class),
                mock(UnderstandingAssessmentRepository.class), new OverallLearningScoreService());
        assertThatThrownBy(() -> service.submitAndEvaluate(1L, "student@example.com",
                new SubmissionRequest("class Main { public static void main(String[] args) { } }")))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Starter code cannot be submitted");
        verify(submissions, never()).save(any());
        verify(attemptWindows, never()).consume(any(), any());
        verifyNoInteractions(judge0, security);
    }

    @Test
    void awardsFunctionalScoreProportionallyForPartialTestPasses() {
        EvaluationFixture fixture = new EvaluationFixture(4);
        when(fixture.judge0.execute(any(), any(), any()))
                .thenReturn(new Judge0ExecutionResult(true, true, "expected-1", null))
                .thenReturn(new Judge0ExecutionResult(true, true, "expected-2", null))
                .thenReturn(new Judge0ExecutionResult(true, true, "wrong", null))
                .thenReturn(new Judge0ExecutionResult(true, true, "wrong", null));

        var result = fixture.evaluate();

        assertThat(result.status()).isEqualTo("FAIL");
        assertThat(result.passedTests()).isEqualTo(2);
        assertThat(result.totalTests()).isEqualTo(4);
        assertThat(result.learningScore()).isEqualTo(28);
        verifyNoInteractions(fixture.security);
    }

    @Test
    void recordsCompilationErrorSeparatelyAndAwardsNoFunctionalPoints() {
        EvaluationFixture fixture = new EvaluationFixture(2);
        when(fixture.judge0.execute(any(), any(), any())).thenReturn(new Judge0ExecutionResult(true, false, null,
                "Main.java: error", Judge0ExecutionStatus.COMPILATION_ERROR));

        var result = fixture.evaluate();

        assertThat(result.status()).isEqualTo("COMPILATION_ERROR");
        assertThat(result.passedTests()).isZero();
        assertThat(result.learningScore()).isEqualTo(15);
        verifyNoInteractions(fixture.security);
    }

    @Test
    void recordsRejectedExecutionAsFailedTestsRatherThanAnInfrastructureFailure() {
        EvaluationFixture fixture = new EvaluationFixture(1);
        when(fixture.judge0.execute(any(), any(), any()))
                .thenReturn(new Judge0ExecutionResult(true, false, null, "Wrong Answer", Judge0ExecutionStatus.REJECTED));

        var result = fixture.evaluate();

        assertThat(result.status()).isEqualTo("FAIL");
        assertThat(result.learningScore()).isEqualTo(15);
    }

    @Test
    void acceptsJudge0OutputThatDiffersOnlyByTransportNewlinesOrTrailingWhitespace() {
        EvaluationFixture fixture = new EvaluationFixture(1);
        when(fixture.judge0.execute(any(), any(), any()))
                .thenReturn(new Judge0ExecutionResult(true, true, "expected-1  \r\n\r\n", null));

        var result = fixture.evaluate();

        assertThat(result.status()).isEqualTo("PASS");
        assertThat(result.passedTests()).isEqualTo(1);
    }

    @Test
    void marksUnavailableJudge0AsUnverifiedAndDoesNotAssignFunctionalPoints() {
        EvaluationFixture fixture = new EvaluationFixture(1);
        when(fixture.judge0.execute(any(), any(), any()))
                .thenReturn(new Judge0ExecutionResult(false, false, null, "Judge0 evaluation timed out",
                        Judge0ExecutionStatus.UNVERIFIED));

        var result = fixture.evaluate();

        assertThat(result.status()).isEqualTo("UNVERIFIED");
        assertThat(result.learningScore()).isEqualTo(15);
        verifyNoInteractions(fixture.security);
    }

    private static final class EvaluationFixture {
        private final ChallengeRepository challenges = mock(ChallengeRepository.class);
        private final UserRepository users = mock(UserRepository.class);
        private final SubmissionRepository submissions = mock(SubmissionRepository.class);
        private final FunctionalEvaluationRepository evaluations = mock(FunctionalEvaluationRepository.class);
        private final Judge0Client judge0 = mock(Judge0Client.class);
        private final SecurityEvaluationService security = mock(SecurityEvaluationService.class);
        private final SecurityCorrectnessScoringService securityCorrectness = mock(SecurityCorrectnessScoringService.class);
        private final IndependentProblemSolvingService independentProblemSolving = mock(IndependentProblemSolvingService.class);
        private final UnderstandingAssessmentRepository understandingAssessments = mock(UnderstandingAssessmentRepository.class);
        private final PrerequisiteAccessService prerequisites = mock(PrerequisiteAccessService.class);
        private final AttemptWindowService attemptWindows = mock(AttemptWindowService.class);
        private final Challenge challenge = new Challenge(1, "Example", "Description", "python", "Injection Attack", "SQL", "CWE-89",
                "starter", Difficulty.EASY);

        private EvaluationFixture(int testCount) {
            challenge.replaceTestCases(java.util.stream.IntStream.rangeClosed(1, testCount)
                    .mapToObj(position -> new TestCase("input-" + position, "expected-" + position, false, position)).toList());
            User user = new User("student@example.com", "unused", Role.STUDENT);
            when(challenges.findById(1L)).thenReturn(Optional.of(challenge));
            when(users.findByEmail("student@example.com")).thenReturn(Optional.of(user));
            when(attemptWindows.consume(user, 1L)).thenReturn(1);
            when(submissions.save(any(Submission.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(evaluations.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(independentProblemSolving.score(any(), any())).thenReturn(new IndependentProblemSolvingResult(15, null));
        }

        private com.securecode.ai.dto.FunctionalEvaluationResponse evaluate() {
            return new FunctionalEvaluationService(challenges, users, submissions, evaluations, judge0, security,
                    new JavaSourceEquivalence(), prerequisites, attemptWindows, securityCorrectness, independentProblemSolving,
                    understandingAssessments, new OverallLearningScoreService())
                    .submitAndEvaluate(1L, "student@example.com", new SubmissionRequest("submitted"));
        }
    }
}
