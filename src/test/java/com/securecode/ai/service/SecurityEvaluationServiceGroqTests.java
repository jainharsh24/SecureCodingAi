package com.securecode.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.Difficulty;
import com.securecode.ai.entity.Role;
import com.securecode.ai.entity.SecurityEvaluation;
import com.securecode.ai.entity.Submission;
import com.securecode.ai.entity.User;
import com.securecode.ai.evaluation.SecurityEvaluationStatus;
import com.securecode.ai.evaluation.SecurityFinding;
import com.securecode.ai.evaluation.common.SecurityFindingEvidenceCodec;
import com.securecode.ai.evaluation.config.SecurityEvaluationProperties;
import com.securecode.ai.evaluation.groq.GroqAssessment;
import com.securecode.ai.evaluation.groq.GroqSecurityEvaluator;
import com.securecode.ai.evaluation.groq.GroqSecurityResult;
import com.securecode.ai.evaluation.semgrep.SemgrepClient;
import com.securecode.ai.evaluation.semgrep.SemgrepScanResult;
import com.securecode.ai.evaluation.semgrep.normalization.SemgrepEvidenceNormalizer;
import com.securecode.ai.evaluation.spotbugs.SpotBugsClient;
import com.securecode.ai.evaluation.spotbugs.SpotBugsScanResult;
import com.securecode.ai.repository.SecurityEvaluationRepository;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

class SecurityEvaluationServiceGroqTests {
    @Test
    void persistsValidatedGroqFindingThroughTheSharedEvidenceCodec() {
        SemgrepClient semgrep = mock(SemgrepClient.class);
        SpotBugsClient spotbugs = mock(SpotBugsClient.class);
        GroqSecurityEvaluator groq = mock(GroqSecurityEvaluator.class);
        SecurityEvaluationRepository repository = mock(SecurityEvaluationRepository.class);
        SecurityFindingEvidenceCodec codec = new SecurityFindingEvidenceCodec(JsonMapper.builder().build());
        Submission submission = submission();
        when(semgrep.scanJava(any())).thenReturn(new SemgrepScanResult(SecurityEvaluationStatus.COMPLETED, List.of(), "{}", null));
        when(spotbugs.scanJava(any())).thenReturn(new SpotBugsScanResult(SecurityEvaluationStatus.COMPLETED, List.of(), null));
        SecurityFinding groqFinding = new SecurityFinding("GROQ_ADVISORY", "Unsafe SQL concatenation", "HIGH",
                "SQL Injection", "CWE-89", "Main.java", 3, null, null, null, null);
        when(groq.evaluate(submission)).thenReturn(new GroqSecurityResult(SecurityEvaluationStatus.COMPLETED,
                new GroqAssessment("VULNERABLE", "YES", "Unsafe SQL concatenation", "HIGH", "CWE-89", .9, "Use parameters"),
                List.of(groqFinding), null));

        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            new SecurityEvaluationService(semgrep, repository, new SemgrepEvidenceNormalizer(), spotbugs, codec, groq,
                    executor, new SecurityEvaluationProperties(3, 1_000)).evaluateJava(submission, submission.getSourceCode());
        } finally { executor.shutdownNow(); }

        ArgumentCaptor<SecurityEvaluation> saved = ArgumentCaptor.forClass(SecurityEvaluation.class);
        verify(repository, org.mockito.Mockito.times(3)).save(saved.capture());
        SecurityEvaluation groqEvaluation = saved.getAllValues().stream()
                .filter(item -> "GROQ".equals(item.getEvaluatorName())).findFirst().orElseThrow();
        assertThat(groqEvaluation.getEvaluatorStatus()).isEqualTo("COMPLETED");
        assertThat(groqEvaluation.isDetected()).isTrue();
        assertThat(groqEvaluation.getAssessment()).isEqualTo("VULNERABLE");
        assertThat(groqEvaluation.getFeedbackSummary()).isEqualTo("Unsafe SQL concatenation");
        assertThat(groqEvaluation.getRemediation()).isEqualTo("Use parameters");
        assertThat(codec.decode(groqEvaluation.getEvidence())).singleElement().satisfies(finding -> {
            assertThat(finding.finding()).isEqualTo("SQL Injection");
            assertThat(finding.cwe()).isEqualTo("CWE-89");
        });
    }

    @Test
    void runsIndependentSecurityEvaluatorsConcurrentlyAndKeepsPartialResults() {
        CountDownLatch started = new CountDownLatch(3);
        CountDownLatch release = new CountDownLatch(1);
        SemgrepClient semgrep = source -> { await(started, release); return new SemgrepScanResult(SecurityEvaluationStatus.COMPLETED, List.of(), "{}", null); };
        SpotBugsClient spotbugs = source -> { await(started, release); return new SpotBugsScanResult(SecurityEvaluationStatus.COMPLETED, List.of(), null); };
        GroqSecurityEvaluator groq = mock(GroqSecurityEvaluator.class);
        when(groq.evaluate(any())).thenAnswer(invocation -> { await(started, release); throw new IllegalStateException("mock provider failure"); });
        SecurityEvaluationRepository repository = mock(SecurityEvaluationRepository.class);
        ExecutorService executor = Executors.newFixedThreadPool(3);
        ExecutorService requestExecutor = Executors.newSingleThreadExecutor();
        try {
            SecurityEvaluationService service = new SecurityEvaluationService(semgrep, repository, new SemgrepEvidenceNormalizer(), spotbugs,
                    new SecurityFindingEvidenceCodec(JsonMapper.builder().build()), groq, executor,
                    new SecurityEvaluationProperties(3, 1_000));
            var request = requestExecutor.submit(() -> service.evaluateJava(submission(), "class Main { }"));
            assertThat(started.await(1, TimeUnit.SECONDS)).isTrue();
            release.countDown();
            request.get(2, TimeUnit.SECONDS);
            ArgumentCaptor<SecurityEvaluation> saved = ArgumentCaptor.forClass(SecurityEvaluation.class);
            verify(repository, org.mockito.Mockito.times(3)).save(saved.capture());
            assertThat(saved.getAllValues()).anySatisfy(item -> {
                assertThat(item.getEvaluatorName()).isEqualTo("GROQ");
                assertThat(item.getEvaluatorStatus()).isEqualTo("ERROR");
            });
        } catch (Exception exception) {
            throw new AssertionError(exception);
        } finally { release.countDown(); requestExecutor.shutdownNow(); executor.shutdownNow(); }
    }

    private static void await(CountDownLatch started, CountDownLatch release) {
        started.countDown();
        try { release.await(2, TimeUnit.SECONDS); }
        catch (InterruptedException exception) { Thread.currentThread().interrupt(); }
    }

    private Submission submission() {
        return new Submission(new User("student@example.com", "unused", Role.STUDENT),
                new Challenge(1, "SQL", "Use prepared statements", "java", "Injection", "SQL Injection", "CWE-89", "", Difficulty.EASY),
                "class Main { }", 1);
    }
}
