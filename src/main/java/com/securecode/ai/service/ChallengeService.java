package com.securecode.ai.service;

import com.securecode.ai.dto.ChallengeRequest;
import com.securecode.ai.dto.ChallengeResponse;
import com.securecode.ai.dto.TestCaseRequest;
import com.securecode.ai.dto.TestCaseResponse;
import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.Role;
import com.securecode.ai.entity.TestCase;
import com.securecode.ai.repository.ChallengeRepository;
import com.securecode.ai.repository.SubmissionRepository;
import com.securecode.ai.entity.User;
import com.securecode.ai.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    public ChallengeService(ChallengeRepository challengeRepository, SubmissionRepository submissionRepository, UserRepository userRepository) { this.challengeRepository = challengeRepository; this.submissionRepository = submissionRepository; this.userRepository = userRepository; }

    @Transactional(readOnly = true)
    public List<ChallengeResponse> getAllForStudent(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        return challengeRepository.findAll().stream().map(c -> toResponse(c, testCaseVisibilityFor(user), user)).toList();
    }
    @Transactional(readOnly = true)
    public ChallengeResponse getByIdForStudent(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        return toResponse(findChallenge(id), testCaseVisibilityFor(user), user);
    }
    @Transactional
    public ChallengeResponse create(ChallengeRequest request) {
        Challenge c = new Challenge(request.title(), request.description(), request.language(), request.vulnerabilityType(),
                request.vulnerabilitySubtype(), request.cwe(), request.starterCode(), request.difficulty());
        c.replaceTestCases(toEntities(request.testCases()));
        return toResponse(challengeRepository.save(c), t -> true, null);
    }
    @Transactional
    public ChallengeResponse update(Long id, ChallengeRequest request) {
        Challenge c = findChallenge(id);
        c.update(request.title(), request.description(), request.language(), request.vulnerabilityType(),
                request.vulnerabilitySubtype(), request.cwe(), request.starterCode(), request.difficulty());
        c.replaceTestCases(toEntities(request.testCases()));
        return toResponse(c, t -> true, null);
    }
    @Transactional
    public void delete(Long id) { challengeRepository.delete(findChallenge(id)); }

    private Challenge findChallenge(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));
    }
    private List<TestCase> toEntities(List<TestCaseRequest> testCases) {
        return testCases.stream().map(t -> new TestCase(t.inputData(), t.expectedOutput(), t.hidden(), t.position())).toList();
    }
    private Predicate<TestCase> testCaseVisibilityFor(User user) {
        return user.getRole() == Role.ADMIN ? testCase -> true : testCase -> !testCase.isHidden();
    }
    private ChallengeResponse toResponse(Challenge c, Predicate<TestCase> include, User user) {
        List<TestCaseResponse> tests = c.getTestCases().stream().filter(include)
                .sorted(Comparator.comparingInt(TestCase::getPosition))
                .map(t -> new TestCaseResponse(t.getId(), t.getInputData(), t.getExpectedOutput(), t.isHidden(), t.getPosition())).toList();
        int attempts = user == null ? 0 : (int) submissionRepository.countByUser_IdAndChallenge_Id(user.getId(), c.getId());
        boolean solved = user != null && submissionRepository.findByUser_EmailAndChallenge_IdOrderByAttemptNumberAsc(user.getEmail(), c.getId())
                .stream().anyMatch(s -> s.getLearningScore() >= 90);
        return new ChallengeResponse(c.getId(), c.getTitle(), c.getDescription(), c.getLanguage(), c.getVulnerabilityType(),
                c.getVulnerabilitySubtype(), c.getCwe(), c.getStarterCode(), tests, c.getDifficulty().name(), attempts, Math.max(0, 5 - attempts), solved);
    }
}
