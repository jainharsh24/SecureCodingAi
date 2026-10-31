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
import com.securecode.ai.repository.UnderstandingAssessmentRepository;
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
    private final SecurityCorrectnessScoringService securityCorrectnessScoringService;
    private final IndependentProblemSolvingService independentProblemSolvingService;
    private final UnderstandingAssessmentRepository understandingAssessments;
    private final OverallLearningScoreService overallLearningScoreService;
    private final JavaSourceEquivalence javaSourceEquivalence;
    private final PrerequisiteAccessService prerequisiteAccess;
    private final AttemptWindowService attemptWindows;

    public FunctionalEvaluationService(ChallengeRepository challengeRepository, UserRepository userRepository,
            SubmissionRepository submissionRepository, FunctionalEvaluationRepository evaluationRepository, Judge0Client judge0Client,
            SecurityEvaluationService securityEvaluationService, JavaSourceEquivalence javaSourceEquivalence, PrerequisiteAccessService prerequisiteAccess,
            AttemptWindowService attemptWindows, SecurityCorrectnessScoringService securityCorrectnessScoringService,
            IndependentProblemSolvingService independentProblemSolvingService,
            UnderstandingAssessmentRepository understandingAssessments, OverallLearningScoreService overallLearningScoreService) {
        this.challengeRepository = challengeRepository; this.userRepository = userRepository;
        this.submissionRepository = submissionRepository; this.evaluationRepository = evaluationRepository; this.judge0Client = judge0Client;
        this.securityEvaluationService = securityEvaluationService;
        this.securityCorrectnessScoringService = securityCorrectnessScoringService;
        this.independentProblemSolvingService = independentProblemSolvingService;
        this.understandingAssessments = understandingAssessments;
        this.overallLearningScoreService = overallLearningScoreService;
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
                if (result.unverified()) {
                    return saveResult(submission, FunctionalEvaluationStatus.UNVERIFIED, passed, testCases.size(), result.details());
                }
                if (result.compilationFailed()) {
                    return saveResult(submission, FunctionalEvaluationStatus.COMPILATION_ERROR, passed, testCases.size(), result.details());
                }
                String expected = normalizeOutput(testCase.getExpectedOutput());
                String actual = normalizeOutput(result.stdout());
                boolean testPassed = result.completed() && result.accepted() && expected.equals(actual);
                if (testPassed) passed++;
                else if (firstFailure == null) firstFailure = result.details() == null || result.details().isBlank()
                        ? "Expected output: " + expected + "; actual output: " + actual : result.details();
            }
        } catch (Judge0UnavailableException | IllegalArgumentException exception) {
            return saveResult(submission, FunctionalEvaluationStatus.UNVERIFIED, passed, testCases.size(), exception.getMessage());
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
        SecurityCorrectnessResult securityCorrectness = securityEvaluation == null ? null
                : securityCorrectnessScoringService.score(submission.getChallenge(),
                        securityEvaluationService.evaluationsFor(submission.getId()));
        if (securityCorrectness != null) {
            submission.recordSecurityCorrectness(securityCorrectness.score(), securityCorrectness.status().name());
        }
        IndependentProblemSolvingResult independent = independentProblemSolvingService.score(submission.getUser().getId(),
                submission.getChallenge().getId());
        submission.recordIndependentProblemSolving(independent.score(),
                independent.highestAssistance() == null ? "NONE" : independent.highestAssistance().name());
        int functionalScore = functionalScore(status, passed, total);
        // Understanding belongs to this exact submission.  Never carry a prior answer
        // forward into a later attempt, otherwise historical/provisional scores drift.
        submission.recordUnderstanding(null, "UNASSESSED");
        var overall = overallLearningScoreService.calculate(status, functionalScore, submission.getSecurityScore(),
                securityCorrectness == null ? null : securityCorrectness.status(),
                null, null,
                independent.score());
        submission.recordOverallLearningScore(overall.displayedScore(), overall.verifiedFinalScore(), overall.scoreState(),
                overall.challengeCompleted(), overall.challengeMastered());
        String rawAssessment = rawAssessment(status, securityEvaluation);
        // rawAssessment is retained for compatibility; learningScore is now the Step 5 aggregate.
        submission.recordAssessment(overall.displayedScore(), rawAssessment);
        return new FunctionalEvaluationResponse(evaluation.getSubmissionId(), evaluation.getStatus().name(),
                evaluation.getPassedTests(), evaluation.getTotalTests(), securityEvaluation, submission.getAttemptNumber(),
                5 - submission.getAttemptNumber(), overall.displayedScore(), submission.getSecurityScore(), submission.getSecurityCorrectnessStatus(),
                submission.getIndependentProblemSolvingScore(), submission.getHighestHintAssistance(), submission.getUnderstandingScore(),
                submission.getUnderstandingAssessmentStatus(), overall.verifiedFinalScore(), overall.provisional(), overall.scoreState(),
                overall.challengeCompleted(), overall.challengeMastered(), submission.getUnderstandingAssessmentSessionToken());
    }

    private String submissionLanguage(Submission submission) { return submission.getLanguage(); }
    private String sourceCode(Submission submission) { return submission.getSourceCode(); }

    private String normalizeOutput(String output) {
        if (output == null) return "";
        String normalized = output.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = normalized.split("\n", -1);
        for (int index = 0; index < lines.length; index++) lines[index] = lines[index].replaceFirst("[ \\t]+$", "");
        int last = lines.length;
        while (last > 0 && lines[last - 1].isEmpty()) last--;
        return String.join("\n", java.util.Arrays.copyOf(lines, last));
    }

    /**
     * Step 1 of the learning score: functionality is worth at most 25 points.
     * It deliberately has no authority to mark a challenge completed or mastered.
     */
    private int functionalScore(FunctionalEvaluationStatus status, int passed, int total) {
        if ((status != FunctionalEvaluationStatus.PASS && status != FunctionalEvaluationStatus.FAIL) || total <= 0) return 0;
        return (int) Math.round(25.0 * passed / total);
    }

    private String rawAssessment(FunctionalEvaluationStatus functional, com.securecode.ai.dto.SecurityEvaluationResponse security) {
        if (functional != FunctionalEvaluationStatus.PASS) return "FAIL";
        // An evaluator outage/unavailable result is not evidence that code is safe.  The
        // persisted security-correctness gate is the authority for the final assessment.
        if (security == null || !"COMPLETED".equals(security.status())) return "NOT_AVAILABLE";
        return security.detected() ? "STRONG VULNERABILITY" : "STRONG SAFE";
    }

    private long elapsedMs(long started) { return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started); }
}
