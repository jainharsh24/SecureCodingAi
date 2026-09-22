package com.securecode.ai.service;

import com.securecode.ai.dto.FunctionalEvaluationResponse;
import com.securecode.ai.dto.SubmissionRequest;
import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.FunctionalEvaluation;
import com.securecode.ai.entity.FunctionalEvaluationStatus;
import com.securecode.ai.entity.Submission;
import com.securecode.ai.entity.TestCase;
import com.securecode.ai.entity.User;
import com.securecode.ai.evaluation.Judge0Client;
import com.securecode.ai.evaluation.Judge0ExecutionResult;
import com.securecode.ai.evaluation.Judge0UnavailableException;
import com.securecode.ai.repository.ChallengeRepository;
import com.securecode.ai.repository.FunctionalEvaluationRepository;
import com.securecode.ai.repository.SubmissionRepository;
import com.securecode.ai.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FunctionalEvaluationService {
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final FunctionalEvaluationRepository evaluationRepository;
    private final Judge0Client judge0Client;
    private final SecurityEvaluationService securityEvaluationService;

    public FunctionalEvaluationService(ChallengeRepository challengeRepository, UserRepository userRepository,
            SubmissionRepository submissionRepository, FunctionalEvaluationRepository evaluationRepository, Judge0Client judge0Client,
            SecurityEvaluationService securityEvaluationService) {
        this.challengeRepository = challengeRepository; this.userRepository = userRepository;
        this.submissionRepository = submissionRepository; this.evaluationRepository = evaluationRepository; this.judge0Client = judge0Client;
        this.securityEvaluationService = securityEvaluationService;
    }

    public FunctionalEvaluationResponse submitAndEvaluate(Long challengeId, String email, SubmissionRequest request) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        Submission submission = submissionRepository.save(new Submission(user, challenge, request.sourceCode()));
        List<TestCase> testCases = challenge.getTestCases().stream()
                .sorted(Comparator.comparingInt(TestCase::getPosition)).toList();
        if (testCases.isEmpty()) return saveResult(submission, FunctionalEvaluationStatus.EVALUATION_ERROR, 0, 0,
                "This challenge has no test cases configured");

        int passed = 0;
        String firstFailure = null;
        try {
            for (TestCase testCase : testCases) {
                Judge0ExecutionResult result = judge0Client.execute(challenge.getLanguage(), request.sourceCode(), testCase.getInputData());
                boolean testPassed = result.completed() && result.accepted()
                        && normalizeOutput(testCase.getExpectedOutput()).equals(normalizeOutput(result.stdout()));
                if (testPassed) passed++;
                else if (firstFailure == null) firstFailure = result.details();
            }
        } catch (Judge0UnavailableException | IllegalArgumentException exception) {
            return saveResult(submission, FunctionalEvaluationStatus.EVALUATION_ERROR, passed, testCases.size(), exception.getMessage());
        }
        FunctionalEvaluationStatus status = passed == testCases.size() ? FunctionalEvaluationStatus.PASS : FunctionalEvaluationStatus.FAIL;
        return saveResult(submission, status, passed, testCases.size(), firstFailure);
    }

    private FunctionalEvaluationResponse saveResult(Submission submission, FunctionalEvaluationStatus status,
            int passed, int total, String details) {
        FunctionalEvaluation evaluation = evaluationRepository.save(new FunctionalEvaluation(submission, status, passed, total, details));
        var securityEvaluation = status == FunctionalEvaluationStatus.PASS && "java".equalsIgnoreCase(submissionLanguage(submission))
                ? securityEvaluationService.evaluateJava(submission, sourceCode(submission)) : null;
        return new FunctionalEvaluationResponse(evaluation.getSubmissionId(), evaluation.getStatus().name(),
                evaluation.getPassedTests(), evaluation.getTotalTests(), securityEvaluation);
    }

    private String submissionLanguage(Submission submission) { return submission.getLanguage(); }
    private String sourceCode(Submission submission) { return submission.getSourceCode(); }

    private String normalizeOutput(String output) {
        if (output == null) return "";
        String normalized = output.replace("\r\n", "\n").replace('\r', '\n');
        while (normalized.endsWith("\n")) normalized = normalized.substring(0, normalized.length() - 1);
        return normalized;
    }
}
