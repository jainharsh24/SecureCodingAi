package com.securecode.ai.service;

import com.securecode.ai.dto.SecurityEvaluationResponse;
import com.securecode.ai.dto.SecurityFindingResponse;
import com.securecode.ai.entity.SecurityEvaluation;
import com.securecode.ai.entity.Submission;
import com.securecode.ai.evaluation.SecurityFinding;
import com.securecode.ai.evaluation.semgrep.SemgrepClient;
import com.securecode.ai.evaluation.semgrep.SemgrepScanResult;
import com.securecode.ai.evaluation.common.SecurityFindingEvidenceCodec;
import com.securecode.ai.evaluation.semgrep.normalization.NormalizedSecurityResult;
import com.securecode.ai.evaluation.semgrep.normalization.SemgrepEvidenceNormalizer;
import com.securecode.ai.evaluation.spotbugs.SpotBugsClient;
import com.securecode.ai.evaluation.spotbugs.SpotBugsScanResult;
import com.securecode.ai.evaluation.groq.GroqSecurityEvaluator;
import com.securecode.ai.evaluation.groq.GroqSecurityResult;
import com.securecode.ai.evaluation.SecurityEvaluationStatus;
import com.securecode.ai.evaluation.config.SecurityEvaluationProperties;
import com.securecode.ai.repository.SecurityEvaluationRepository;
import java.util.List;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class SecurityEvaluationService {
    private static final Logger log = LoggerFactory.getLogger(SecurityEvaluationService.class);
    private static final String SEMGREP = "SEMGREP";
    private static final String SPOTBUGS = "SPOTBUGS";
    private final SemgrepClient semgrepClient;
    private final SpotBugsClient spotBugsClient;
    private final SecurityEvaluationRepository securityEvaluationRepository;
    private final SemgrepEvidenceNormalizer semgrepEvidenceNormalizer;
    private final SecurityFindingEvidenceCodec findingEvidenceCodec;
    private final GroqSecurityEvaluator groqSecurityEvaluator;
    private final ExecutorService securityEvaluatorExecutor;
    private final SecurityEvaluationProperties executionProperties;

    public SecurityEvaluationService(SemgrepClient semgrepClient, SecurityEvaluationRepository securityEvaluationRepository,
            SemgrepEvidenceNormalizer semgrepEvidenceNormalizer, SpotBugsClient spotBugsClient,
            SecurityFindingEvidenceCodec findingEvidenceCodec, GroqSecurityEvaluator groqSecurityEvaluator,
            @Qualifier("securityEvaluatorExecutor") ExecutorService securityEvaluatorExecutor,
            SecurityEvaluationProperties executionProperties) {
        this.semgrepClient = semgrepClient;
        this.securityEvaluationRepository = securityEvaluationRepository;
        this.semgrepEvidenceNormalizer = semgrepEvidenceNormalizer;
        this.spotBugsClient = spotBugsClient;
        this.findingEvidenceCodec = findingEvidenceCodec;
        this.groqSecurityEvaluator = groqSecurityEvaluator;
        this.securityEvaluatorExecutor = securityEvaluatorExecutor;
        this.executionProperties = executionProperties;
    }

    public SecurityEvaluationResponse evaluateJava(Submission submission, String sourceCode) {
        long started = System.nanoTime();
        Long submissionId = submission.getId();
        Future<SemgrepScanResult> semgrep = submit("SEMGREP", submissionId, () -> semgrepClient.scanJava(sourceCode), SemgrepScanResult::status);
        Future<SpotBugsScanResult> spotbugs = submit("SPOTBUGS", submissionId, () -> spotBugsClient.scanJava(sourceCode), SpotBugsScanResult::status);
        Future<GroqSecurityResult> groq = submit("GROQ", submissionId, () -> groqSecurityEvaluator.evaluate(submission), GroqSecurityResult::status);

        SemgrepScanResult result = resolve("SEMGREP", submissionId, semgrep,
                message -> new SemgrepScanResult(SecurityEvaluationStatus.TIMEOUT, List.of(), null, message),
                message -> new SemgrepScanResult(SecurityEvaluationStatus.ERROR, List.of(), null, message));
        NormalizedSecurityResult normalized = semgrepEvidenceNormalizer.normalize(result);
        persist(submission, SEMGREP, result.findings(), normalized.status().name(), normalized.error());

        SpotBugsScanResult spotBugsResult = resolve("SPOTBUGS", submissionId, spotbugs,
                message -> new SpotBugsScanResult(SecurityEvaluationStatus.TIMEOUT, List.of(), message),
                message -> new SpotBugsScanResult(SecurityEvaluationStatus.ERROR, List.of(), message));
        persist(submission, SPOTBUGS, spotBugsResult.findings(), spotBugsResult.status().name(), spotBugsResult.error());

        GroqSecurityResult groqResult = resolve("GROQ", submissionId, groq,
                message -> new GroqSecurityResult(SecurityEvaluationStatus.TIMEOUT, null, List.of(), message),
                message -> new GroqSecurityResult(SecurityEvaluationStatus.ERROR, null, List.of(), message));
        persistGroq(submission, groqResult);

        log.info("security evaluation completed submissionId={} durationMs={}", submissionId, elapsedMs(started));

        // Keep the established submission response shape: it remains the Semgrep response.
        return new SecurityEvaluationResponse(SEMGREP, normalized.status().name(), !normalized.findings().isEmpty(),
                result.findings().stream().map(this::toResponse).toList(), normalized.error(), normalized);
    }

    /** Reads the evaluator records persisted by the established evaluation pipeline. */
    public List<SecurityEvaluation> evaluationsFor(Long submissionId) {
        return securityEvaluationRepository.findBySubmission_Id(submissionId);
    }

    private <T> Future<T> submit(String evaluator, Long submissionId, Supplier<T> operation,
            Function<T, SecurityEvaluationStatus> status) {
        try {
            return securityEvaluatorExecutor.submit(() -> {
            long started = System.nanoTime();
            try {
                T result = operation.get();
                log.info("security evaluator completed evaluator={} submissionId={} status={} durationMs={}",
                        evaluator, submissionId, status.apply(result), elapsedMs(started));
                return result;
            } catch (RuntimeException exception) {
                log.warn("security evaluator failed evaluator={} submissionId={} durationMs={}", evaluator, submissionId,
                        elapsedMs(started), exception);
                throw exception;
            }
            });
        } catch (RejectedExecutionException exception) {
            log.warn("security evaluator was not scheduled evaluator={} submissionId={}", evaluator, submissionId);
            return CompletableFuture.failedFuture(exception);
        }
    }

    private <T> T resolve(String evaluator, Long submissionId, Future<T> future,
            Function<String, T> timeoutResult, Function<String, T> errorResult) {
        try {
            return future.get(executionProperties.timeoutMs(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException exception) {
            future.cancel(true);
            String message = evaluator + " exceeded " + executionProperties.timeoutMs() + " ms";
            log.warn("security evaluator timed out evaluator={} submissionId={} durationMs={}", evaluator, submissionId,
                    executionProperties.timeoutMs());
            return timeoutResult.apply(message);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return errorResult.apply(evaluator + " evaluation was interrupted");
        } catch (ExecutionException exception) {
            String message = exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage();
            log.warn("security evaluator returned an unhandled error evaluator={} submissionId={}", evaluator, submissionId,
                    exception.getCause());
            return errorResult.apply(message == null ? evaluator + " evaluation failed" : message);
        }
    }

    private long elapsedMs(long started) { return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started); }

    private void persist(Submission submission, String evaluator, List<SecurityFinding> findings, String status, String error) {
        SecurityFinding firstFinding = findings.isEmpty() ? null : findings.getFirst();
        String severity = firstFinding == null && "COMPLETED".equals(status) ? "INFO"
                : firstFinding == null ? "EVALUATOR_ERROR" : firstFinding.severity();
        securityEvaluationRepository.save(new SecurityEvaluation(submission, evaluator, !findings.isEmpty(),
                firstFinding == null ? null : firstFinding.vulnerabilityType(), firstFinding == null ? null : firstFinding.cwe(),
                severity, null, findingEvidenceCodec.encode(findings), status, error));
    }

    private void persistGroq(Submission submission, GroqSecurityResult result) {
        SecurityFinding firstFinding = result.findings().isEmpty() ? null : result.findings().getFirst();
        var assessment = result.assessment();
        String severity = firstFinding == null && "COMPLETED".equals(result.status().name()) ? "INFO"
                : firstFinding == null ? "EVALUATOR_ERROR" : firstFinding.severity();
        securityEvaluationRepository.save(new SecurityEvaluation(submission, "GROQ", !result.findings().isEmpty(),
                firstFinding == null ? null : firstFinding.vulnerabilityType(), firstFinding == null ? null : firstFinding.cwe(),
                severity, assessment == null ? null : BigDecimal.valueOf(assessment.confidence()),
                findingEvidenceCodec.encode(result.findings()), result.status().name(), result.error(),
                assessment == null ? null : assessment.status(), assessment == null ? null : assessment.remediation(),
                assessment == null ? null : assessment.evidence()));
    }

    private SecurityFindingResponse toResponse(SecurityFinding finding) {
        return new SecurityFindingResponse(finding.ruleId(), finding.message(), finding.severity(), finding.vulnerabilityType(),
                finding.cwe(), finding.startLine(), finding.code());
    }
}
