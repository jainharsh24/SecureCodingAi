package com.securecode.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.securecode.ai.dto.SubmissionAnalysisResponse;
import com.securecode.ai.entity.FunctionalEvaluation;
import com.securecode.ai.entity.FunctionalEvaluationStatus;
import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.Difficulty;
import com.securecode.ai.entity.Role;
import com.securecode.ai.entity.SecurityEvaluation;
import com.securecode.ai.entity.Submission;
import com.securecode.ai.entity.UnderstandingAssessment;
import com.securecode.ai.entity.User;
import com.securecode.ai.evaluation.common.SecurityFindingEvidenceCodec;
import com.securecode.ai.repository.FunctionalEvaluationRepository;
import com.securecode.ai.repository.SecurityEvaluationRepository;
import com.securecode.ai.repository.SubmissionRepository;
import com.securecode.ai.repository.UnderstandingAssessmentRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SubmissionAnalysisServiceTests {

    @Test
    void returnsOnlyTheExplanationAssociatedWithTheSelectedSubmission() {
        SubmissionRepository submissions = Mockito.mock(SubmissionRepository.class);
        FunctionalEvaluationRepository functionalEvaluations = Mockito.mock(FunctionalEvaluationRepository.class);
        SecurityEvaluationRepository securityEvaluations = Mockito.mock(SecurityEvaluationRepository.class);
        UnderstandingAssessmentRepository understandingAssessments = Mockito.mock(UnderstandingAssessmentRepository.class);
        SecurityFindingEvidenceCodec codec = Mockito.mock(SecurityFindingEvidenceCodec.class);
        Submission submission = Mockito.mock(Submission.class);
        User user = Mockito.mock(User.class);
        UnderstandingAssessment selectedExplanation = Mockito.mock(UnderstandingAssessment.class);
        Challenge challenge = new Challenge(18, "Saved explanation", "Description", "java", null, null, null, null, Difficulty.EASY);
        when(user.getEmail()).thenReturn("student@example.com");
        when(user.getId()).thenReturn(42L);
        when(submission.getUser()).thenReturn(user);
        when(submission.getChallenge()).thenReturn(challenge);
        when(submission.getLanguage()).thenReturn("java");
        when(submission.getSourceCode()).thenReturn("class Main { }");
        when(submissions.findById(21L)).thenReturn(Optional.of(submission));
        when(functionalEvaluations.findBySubmission_Id(21L))
                .thenReturn(Optional.of(new FunctionalEvaluation(submission, FunctionalEvaluationStatus.PASS, 1, 1, null)));
        when(securityEvaluations.findBySubmission_Id(21L)).thenReturn(List.of());
        when(understandingAssessments.findFirstByUser_IdAndSubmission_IdOrderByIdDesc(42L, 21L))
                .thenReturn(Optional.of(selectedExplanation));
        when(selectedExplanation.getAnswer()).thenReturn("This explanation belongs to submission 21.");

        SubmissionAnalysisResponse response = new SubmissionAnalysisService(submissions, functionalEvaluations,
                securityEvaluations, codec, null, understandingAssessments).getForStudent(21L, "student@example.com");

        assertThat(response.submittedExplanation()).isEqualTo("This explanation belongs to submission 21.");
    }

    @Test
    void returnsPersistedSemgrepEvidenceAndUnavailableFutureEvaluators() {
        SubmissionRepository submissions = Mockito.mock(SubmissionRepository.class);
        FunctionalEvaluationRepository functionalEvaluations = Mockito.mock(FunctionalEvaluationRepository.class);
        SecurityEvaluationRepository securityEvaluations = Mockito.mock(SecurityEvaluationRepository.class);
        SecurityFindingEvidenceCodec findingEvidenceCodec = Mockito.mock(SecurityFindingEvidenceCodec.class);
        Submission submission = Mockito.mock(Submission.class);
        Challenge challenge = new Challenge(12, "Saved challenge", "Original full description", "java", null, null, null, null, Difficulty.MEDIUM);
        User user = new User("student@example.com", "unused", Role.STUDENT);
        when(submission.getUser()).thenReturn(user);
        when(submission.getChallenge()).thenReturn(challenge);
        when(submission.getLanguage()).thenReturn("java");
        when(submission.getSourceCode()).thenReturn("class Main { }");
        when(submissions.findById(7L)).thenReturn(Optional.of(submission));
        when(functionalEvaluations.findBySubmission_Id(7L))
                .thenReturn(Optional.of(new FunctionalEvaluation(submission, FunctionalEvaluationStatus.PASS, 2, 2, null)));
        when(securityEvaluations.findBySubmission_Id(7L)).thenReturn(List.of(
                new SecurityEvaluation(submission, "SEMGREP", false, null, null, "INFO", null, "{}", "COMPLETED", null),
                new SecurityEvaluation(submission, "SPOTBUGS", false, null, null, "INFO", null, "{}", "COMPLETED", null),
                new SecurityEvaluation(submission, "GROQ", false, null, null, "INFO", null, "{}", "COMPLETED", null,
                        "SAFE", "Keep using parameter binding for every query path.",
                        "You used parameter binding, so user input is kept separate from SQL syntax.")));

        SubmissionAnalysisResponse response = new SubmissionAnalysisService(submissions, functionalEvaluations,
                securityEvaluations, findingEvidenceCodec).getForStudent(7L, "student@example.com");

        assertThat(response.overallDecision()).isEqualTo("SECURITY PASS");
        assertThat(response.assessment().evidenceState()).isEqualTo("STRONG SAFE EVIDENCE");
        assertThat(response.evidenceSummary().safeEvidencePercent()).isEqualTo(100);
        assertThat(response.evidenceSummary().safeCount()).isEqualTo(3);
        assertThat(response.submissionContext()).satisfies(context -> {
            assertThat(context.challengeNumber()).isEqualTo(12);
            assertThat(context.challengeDescription()).isEqualTo("Original full description");
            assertThat(context.submittedCode()).isEqualTo("class Main { }");
        });
        assertThat(response.evaluators()).extracting(item -> item.evaluator() + ":" + item.result())
                .containsExactly("JUDGE0:PASS", "SEMGREP:SAFE", "SPOTBUGS:SAFE", "GROQ:SAFE");
        assertThat(response.evaluators()).filteredOn(item -> item.evaluator().equals("GROQ"))
                .extracting(item -> item.displayName()).containsExactly("LLM");
        assertThat(response.learningFeedback()).satisfies(feedback -> {
            assertThat(feedback.state()).isEqualTo("SAFE");
            assertThat(feedback.summary()).contains("parameter binding");
            assertThat(feedback.guidance()).contains("every query path");
            assertThat(feedback.available()).isTrue();
        });
    }

    @Test
    void doesNotIssueSecurityVerdictWhenFunctionalEvaluationFails() {
        SubmissionRepository submissions = Mockito.mock(SubmissionRepository.class);
        FunctionalEvaluationRepository functionalEvaluations = Mockito.mock(FunctionalEvaluationRepository.class);
        SecurityEvaluationRepository securityEvaluations = Mockito.mock(SecurityEvaluationRepository.class);
        SecurityFindingEvidenceCodec findingEvidenceCodec = Mockito.mock(SecurityFindingEvidenceCodec.class);
        Submission submission = Mockito.mock(Submission.class);
        Challenge challenge = new Challenge(13, "Failed challenge", "Full description", "java", null, null, null, null, Difficulty.EASY);
        when(submission.getUser()).thenReturn(new User("student@example.com", "unused", Role.STUDENT));
        when(submission.getChallenge()).thenReturn(challenge);
        when(submission.getLanguage()).thenReturn("java");
        when(submission.getSourceCode()).thenReturn("class Main { }");
        when(submissions.findById(8L)).thenReturn(Optional.of(submission));
        when(functionalEvaluations.findBySubmission_Id(8L))
                .thenReturn(Optional.of(new FunctionalEvaluation(submission, FunctionalEvaluationStatus.FAIL, 1, 2, null)));
        when(securityEvaluations.findBySubmission_Id(8L)).thenReturn(List.of());

        SubmissionAnalysisResponse response = new SubmissionAnalysisService(submissions, functionalEvaluations,
                securityEvaluations, findingEvidenceCodec).getForStudent(8L, "student@example.com");

        assertThat(response.overallDecision()).isEqualTo("Functional Fail");
        assertThat(response.assessment().evidenceState()).isEqualTo("Functional Fail");
        assertThat(response.evaluators()).filteredOn(item -> item.evaluator().equals("GROQ"))
                .extracting(item -> item.result()).containsExactly("NOT_RUN");
    }

    @Test
    void usesPersistedConflictGateInsteadOfAveragingItIntoASecurityPass() {
        SubmissionRepository submissions = Mockito.mock(SubmissionRepository.class);
        FunctionalEvaluationRepository functionalEvaluations = Mockito.mock(FunctionalEvaluationRepository.class);
        SecurityEvaluationRepository securityEvaluations = Mockito.mock(SecurityEvaluationRepository.class);
        SecurityFindingEvidenceCodec codec = Mockito.mock(SecurityFindingEvidenceCodec.class);
        Submission submission = Mockito.mock(Submission.class);
        Challenge challenge = new Challenge(15, "Conflict", "Description", "java", null, null, null, null, Difficulty.EASY);
        when(submission.getUser()).thenReturn(new User("student@example.com", "unused", Role.STUDENT));
        when(submission.getChallenge()).thenReturn(challenge);
        when(submission.getLanguage()).thenReturn("java");
        when(submission.getSourceCode()).thenReturn("class Main { }");
        when(submission.getSecurityCorrectnessStatus()).thenReturn("CONFLICTING_EVIDENCE");
        when(submissions.findById(10L)).thenReturn(Optional.of(submission));
        when(functionalEvaluations.findBySubmission_Id(10L))
                .thenReturn(Optional.of(new FunctionalEvaluation(submission, FunctionalEvaluationStatus.PASS, 1, 1, null)));
        when(securityEvaluations.findBySubmission_Id(10L)).thenReturn(List.of(
                new SecurityEvaluation(submission, "SEMGREP", false, null, null, "INFO", null, "{}", "COMPLETED", null),
                new SecurityEvaluation(submission, "SPOTBUGS", true, "Injection", "CWE-89", "HIGH", null, "{}", "COMPLETED", null)));

        SubmissionAnalysisResponse response = new SubmissionAnalysisService(submissions, functionalEvaluations,
                securityEvaluations, codec).getForStudent(10L, "student@example.com");

        assertThat(response.overallDecision()).isEqualTo("INCONCLUSIVE");
        assertThat(response.assessment().evidenceState()).isEqualTo("CONFLICTING EVIDENCE");
    }

    @Test
    void marksUncertainGroqAdviceAsUncertainRatherThanSafeEvidence() {
        SubmissionRepository submissions = Mockito.mock(SubmissionRepository.class);
        FunctionalEvaluationRepository functionalEvaluations = Mockito.mock(FunctionalEvaluationRepository.class);
        SecurityEvaluationRepository securityEvaluations = Mockito.mock(SecurityEvaluationRepository.class);
        SecurityFindingEvidenceCodec codec = Mockito.mock(SecurityFindingEvidenceCodec.class);
        Submission submission = Mockito.mock(Submission.class);
        Challenge challenge = new Challenge(14, "Uncertain challenge", "Full description", "java", null, null, null, null, Difficulty.EASY);
        when(submission.getUser()).thenReturn(new User("student@example.com", "unused", Role.STUDENT));
        when(submission.getChallenge()).thenReturn(challenge);
        when(submission.getLanguage()).thenReturn("java");
        when(submission.getSourceCode()).thenReturn("class Main { }");
        when(submissions.findById(9L)).thenReturn(Optional.of(submission));
        when(functionalEvaluations.findBySubmission_Id(9L))
                .thenReturn(Optional.of(new FunctionalEvaluation(submission, FunctionalEvaluationStatus.PASS, 1, 1, null)));
        when(securityEvaluations.findBySubmission_Id(9L)).thenReturn(List.of(
                new SecurityEvaluation(submission, "GROQ", false, null, null, "INFO", null, "{}", "COMPLETED", null,
                        "UNCERTAIN", "Inspect every input-to-sink path and use the challenge's safe API.",
                        "The available code does not establish whether every path handles untrusted input safely.")));

        SubmissionAnalysisResponse response = new SubmissionAnalysisService(submissions, functionalEvaluations,
                securityEvaluations, codec).getForStudent(9L, "student@example.com");

        assertThat(response.evaluators()).filteredOn(item -> item.evaluator().equals("GROQ"))
                .extracting(item -> item.result()).containsExactly("UNCERTAIN");
        assertThat(response.evidenceSummary().safeCount()).isZero();
        assertThat(response.learningFeedback().state()).isEqualTo("UNCERTAIN");
    }

    @Test
    void doesNotRenderStatusLessLegacyEvaluatorRowsAsSafe() {
        SubmissionRepository submissions = Mockito.mock(SubmissionRepository.class);
        FunctionalEvaluationRepository functionalEvaluations = Mockito.mock(FunctionalEvaluationRepository.class);
        SecurityEvaluationRepository securityEvaluations = Mockito.mock(SecurityEvaluationRepository.class);
        SecurityFindingEvidenceCodec codec = Mockito.mock(SecurityFindingEvidenceCodec.class);
        Submission submission = Mockito.mock(Submission.class);
        Challenge challenge = new Challenge(16, "Legacy scan", "Description", "java", null, null, null, null, Difficulty.EASY);
        when(submission.getUser()).thenReturn(new User("student@example.com", "unused", Role.STUDENT));
        when(submission.getChallenge()).thenReturn(challenge);
        when(submission.getLanguage()).thenReturn("java");
        when(submission.getSourceCode()).thenReturn("class Main { }");
        when(submissions.findById(11L)).thenReturn(Optional.of(submission));
        when(functionalEvaluations.findBySubmission_Id(11L))
                .thenReturn(Optional.of(new FunctionalEvaluation(submission, FunctionalEvaluationStatus.PASS, 1, 1, null)));
        when(securityEvaluations.findBySubmission_Id(11L)).thenReturn(List.of(
                new SecurityEvaluation(submission, "SEMGREP", false, null, null, "INFO", null, "{}", null, "legacy failure")));

        SubmissionAnalysisResponse response = new SubmissionAnalysisService(submissions, functionalEvaluations,
                securityEvaluations, codec).getForStudent(11L, "student@example.com");

        assertThat(response.evaluators()).filteredOn(item -> item.evaluator().equals("SEMGREP"))
                .extracting(item -> item.result()).containsExactly("UNVERIFIED");
        assertThat(response.evaluators()).filteredOn(item -> item.evaluator().equals("SEMGREP"))
                .allSatisfy(item -> assertThat(item.available()).isFalse());
    }

    @Test
    void marksTheAssessmentUnverifiedAndReturnsNoPercentagesWhenNoEvaluatorHasAValidVerdict() {
        SubmissionRepository submissions = Mockito.mock(SubmissionRepository.class);
        FunctionalEvaluationRepository functionalEvaluations = Mockito.mock(FunctionalEvaluationRepository.class);
        SecurityEvaluationRepository securityEvaluations = Mockito.mock(SecurityEvaluationRepository.class);
        SecurityFindingEvidenceCodec codec = Mockito.mock(SecurityFindingEvidenceCodec.class);
        Submission submission = Mockito.mock(Submission.class);
        Challenge challenge = new Challenge(17, "Unavailable scan", "Description", "java", null, null, null, null, Difficulty.EASY);
        when(submission.getUser()).thenReturn(new User("student@example.com", "unused", Role.STUDENT));
        when(submission.getChallenge()).thenReturn(challenge);
        when(submission.getLanguage()).thenReturn("java");
        when(submission.getSourceCode()).thenReturn("class Main { }");
        when(submissions.findById(12L)).thenReturn(Optional.of(submission));
        when(functionalEvaluations.findBySubmission_Id(12L))
                .thenReturn(Optional.of(new FunctionalEvaluation(submission, FunctionalEvaluationStatus.PASS, 1, 1, null)));
        when(securityEvaluations.findBySubmission_Id(12L)).thenReturn(List.of(
                new SecurityEvaluation(submission, "SEMGREP", false, null, null, "EVALUATOR_ERROR", null, "{}", "TIMEOUT", "timed out"),
                new SecurityEvaluation(submission, "GROQ", false, null, null, "INFO", null, "{}", "COMPLETED", null,
                        null, null, null)));

        SubmissionAnalysisResponse response = new SubmissionAnalysisService(submissions, functionalEvaluations,
                securityEvaluations, codec).getForStudent(12L, "student@example.com");

        assertThat(response.evidenceSummary().validEvaluatorCount()).isZero();
        assertThat(response.evidenceSummary().safeEvidencePercent()).isZero();
        assertThat(response.evidenceSummary().vulnerabilityEvidencePercent()).isZero();
        assertThat(response.assessment().evidenceState()).isEqualTo("UNVERIFIED");
        assertThat(response.evaluators()).filteredOn(item -> item.evaluator().equals("GROQ"))
                .extracting(item -> item.result()).containsExactly("UNVERIFIED");
    }
}
