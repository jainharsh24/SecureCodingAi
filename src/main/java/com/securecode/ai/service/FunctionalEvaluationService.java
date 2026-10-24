package com.securecode.ai.service;

import com.securecode.ai.dto.FunctionalEvaluationResponse;
import com.securecode.ai.dto.SubmissionRequest;
import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.FunctionalEvaluation;
import com.securecode.ai.entity.FunctionalEvaluationStatus;
import com.securecode.ai.entity.Submission;
import com.securecode.ai.entity.TestCase;
import com.securecode.ai.entity.User;
import com.securecode.ai.evaluation.judge0.Judge0Client;
import com.securecode.ai.evaluation.judge0.Judge0ExecutionResult;
import com.securecode.ai.evaluation.judge0.Judge0UnavailableException;
import com.securecode.ai.repository.ChallengeRepository;
import com.securecode.ai.repository.FunctionalEvaluationRepository;
import com.securecode.ai.repository.SubmissionRepository;
import com.securecode.ai.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FunctionalEvaluationService {
    private static final Logger log = LoggerFactory.getLogger(FunctionalEvaluationService.class);
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final FunctionalEvaluationRepository evaluationRepository;
    private final Judge0Client judge0Client;
    private final SecurityEvaluationService securityEvaluationService;
    private final JavaSourceEquivalence javaSourceEquivalence;
    private final PrerequisiteAccessService prerequisiteAccess;
    private final AttemptWindowService attemptWindows;

    public FunctionalEvaluationService(ChallengeRepository challengeRepository, UserRepository userRepository,
            SubmissionRepository submissionRepository, FunctionalEvaluationRepository evaluationRepository, Judge0Client judge0Client,
            SecurityEvaluationService securityEvaluationService, JavaSourceEquivalence javaSourceEquivalence, PrerequisiteAccessService prerequisiteAccess,
            AttemptWindowService attemptWindows) {
        this.challengeRepository = challengeRepository; this.userRepository = userRepository;
        this.submissionRepository = submissionRepository; this.evaluationRepository = evaluationRepository; this.judge0Client = judge0Client;
        this.securityEvaluationService = securityEvaluationService;
        this.javaSourceEquivalence = javaSourceEquivalence;
        this.prerequisiteAccess = prerequisiteAccess;
        this.attemptWindows = attemptWindows;
    }

    @Transactional
    public FunctionalEvaluationResponse submitAndEvaluate(Long challengeId, String email, SubmissionRequest request) {
        long overallStarted = System.nanoTime();
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));
        if (!challenge.isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge is no longer in the active catalog");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        prerequisiteAccess.requireAccess(user, challenge, java.util.Map.of());
        if (isUnchangedStarter(challenge, request.sourceCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Starter code cannot be submitted. Modify the code before submitting.");
        }
        int attemptNumber = attemptWindows.consume(user, challengeId);
        Submission submission = submissionRepository.save(new Submission(user, challenge, request.sourceCode(), attemptNumber));
        List<TestCase> testCases = challenge.getTestCases().stream()
                .sorted(Comparator.comparingInt(TestCase::getPosition)).toList();
        if (testCases.isEmpty()) return saveResult(submission, FunctionalEvaluationStatus.EVALUATION_ERROR, 0, 0,
                "This challenge has no test cases configured");

        int passed = 0;
        String firstFailure = null;
        try {
            for (TestCase testCase : testCases) {
                long judge0Started = System.nanoTime();
                Judge0ExecutionResult result = judge0Client.execute(challenge.getLanguage(), request.sourceCode(), testCase.getInputData());
                log.info("functional evaluator completed evaluator=JUDGE0 submissionId={} status={} durationMs={}", submission.getId(),
                        result.completed() && result.accepted() ? "COMPLETED" : "FAILED", elapsedMs(judge0Started));
                boolean testPassed = result.completed() && result.accepted()
                        && normalizeOutput(testCase.getExpectedOutput()).equals(normalizeOutput(result.stdout()));
                if (testPassed) passed++;
                else if (firstFailure == null) firstFailure = result.details();
            }
        } catch (Judge0UnavailableException | IllegalArgumentException exception) {
            return saveResult(submission, FunctionalEvaluationStatus.EVALUATION_ERROR, passed, testCases.size(), exception.getMessage());
        }
        FunctionalEvaluationStatus status = passed == testCases.size() ? FunctionalEvaluationStatus.PASS : FunctionalEvaluationStatus.FAIL;
        FunctionalEvaluationResponse response = saveResult(submission, status, passed, testCases.size(), firstFailure);
        log.info("submission evaluation completed submissionId={} status={} durationMs={}", submission.getId(), status,
                elapsedMs(overallStarted));
        return response;
    }

    private boolean isUnchangedStarter(Challenge challenge, String submittedCode) {
        String starterCode = challenge.getStarterCode();
        if (starterCode == null || starterCode.isBlank()) return false;
        return "java".equalsIgnoreCase(challenge.getLanguage())
                ? javaSourceEquivalence.equivalent(starterCode, submittedCode)
                : starterCode.equals(submittedCode);
    }

    private FunctionalEvaluationResponse saveResult(Submission submission, FunctionalEvaluationStatus status,
            int passed, int total, String details) {
        FunctionalEvaluation evaluation = evaluationRepository.save(new FunctionalEvaluation(submission, status, passed, total, details));
        var securityEvaluation = status == FunctionalEvaluationStatus.PASS && "java".equalsIgnoreCase(submissionLanguage(submission))
                ? securityEvaluationService.evaluateJava(submission, sourceCode(submission)) : null;
        int score = learningScore(status, securityEvaluation);
        String rawAssessment = rawAssessment(status, securityEvaluation);
        submission.recordAssessment(score, rawAssessment);
        return new FunctionalEvaluationResponse(evaluation.getSubmissionId(), evaluation.getStatus().name(),
                evaluation.getPassedTests(), evaluation.getTotalTests(), securityEvaluation, submission.getAttemptNumber(),
                5 - submission.getAttemptNumber(), score);
    }

    private String submissionLanguage(Submission submission) { return submission.getLanguage(); }
    private String sourceCode(Submission submission) { return submission.getSourceCode(); }

    private String normalizeOutput(String output) {
        if (output == null) return "";
        String normalized = output.replace("\r\n", "\n").replace('\r', '\n');
        while (normalized.endsWith("\n")) normalized = normalized.substring(0, normalized.length() - 1);
        return normalized;
    }

    private int learningScore(FunctionalEvaluationStatus functional, com.securecode.ai.dto.SecurityEvaluationResponse security) {
        if (functional != FunctionalEvaluationStatus.PASS) return 0;
        if (security == null || !"COMPLETED".equals(security.status())) return 70;
        // Semgrep's completed finding set is the persisted security evidence.  No findings is strong safe evidence.
        if (!security.detected()) return 100;
        return 25;
    }

    private String rawAssessment(FunctionalEvaluationStatus functional, com.securecode.ai.dto.SecurityEvaluationResponse security) {
        if (functional != FunctionalEvaluationStatus.PASS) return "FAIL";
        if (security == null || !"COMPLETED".equals(security.status())) return "MOSTLY SAFE";
        return security.detected() ? "STRONG VULNERABILITY" : "STRONG SAFE";
    }

    private long elapsedMs(long started) { return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started); }
}
