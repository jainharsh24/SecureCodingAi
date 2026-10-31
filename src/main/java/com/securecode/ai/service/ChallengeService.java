package com.securecode.ai.service;

import com.securecode.ai.dto.ChallengeRequest;
import com.securecode.ai.dto.ChallengeResponse;
import com.securecode.ai.dto.PrerequisiteStatusResponse;
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
import java.util.ArrayList;
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
    private final PrerequisiteAccessService prerequisiteAccess;
    public ChallengeService(ChallengeRepository challengeRepository, SubmissionRepository submissionRepository, UserRepository userRepository,
            PrerequisiteAccessService prerequisiteAccess) { this.challengeRepository = challengeRepository; this.submissionRepository = submissionRepository; this.userRepository = userRepository; this.prerequisiteAccess = prerequisiteAccess; }

    @Transactional(readOnly = true)
    public List<ChallengeResponse> getAllForStudent(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        List<ChallengeResponse> catalog = new ArrayList<>();
        List<Challenge> activeChallenges = challengeRepository.findByActiveTrueOrderByChallengeNumberAsc();
        var catalogNumbers = catalogNumbers(activeChallenges);
        for (int index = 0; index < activeChallenges.size(); index++) {
            catalog.add(toResponse(activeChallenges.get(index), testCaseVisibilityFor(user), user, index + 1, catalogNumbers));
        }
        return List.copyOf(catalog);
    }
    @Transactional(readOnly = true)
    public ChallengeResponse getByIdForStudent(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        Challenge challenge = findChallenge(id);
        Integer catalogNumber = null;
        if (challenge.isActive()) {
            List<Challenge> activeChallenges = challengeRepository.findByActiveTrueOrderByChallengeNumberAsc();
            for (int index = 0; index < activeChallenges.size(); index++) {
                if (activeChallenges.get(index).getId().equals(challenge.getId())) {
                    catalogNumber = index + 1;
                    break;
                }
            }
        }
        var catalogNumbers = catalogNumbers(challengeRepository.findByActiveTrueOrderByChallengeNumberAsc());
        return toResponse(challenge, testCaseVisibilityFor(user), user, catalogNumber, catalogNumbers);
    }
    @Transactional(readOnly = true)
    public com.securecode.ai.dto.PrerequisiteAccessResponse prerequisiteStatus(Long id, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        return prerequisiteAccess.access(user, findChallenge(id), catalogNumbers(challengeRepository.findByActiveTrueOrderByChallengeNumberAsc()));
    }
    @Transactional
    public ChallengeResponse create(ChallengeRequest request) {
        long challengeNumber = challengeRepository.nextChallengeNumber();
        var prereqs = resolveAndValidatePrerequisites(null, challengeNumber, request.prerequisiteChallengeNumbers());
        Challenge c = new Challenge(challengeNumber, request.title(), request.description(), request.language(), request.vulnerabilityType(),
                request.vulnerabilitySubtype(), request.cwe(), request.starterCode(), request.difficulty());
        c.replaceTestCases(toEntities(request.testCases()));
        c.replacePrerequisites(prereqs);
        Challenge saved = challengeRepository.save(c);
        var catalog = challengeRepository.findByActiveTrueOrderByChallengeNumberAsc();
        var catalogNumbers = catalogNumbers(catalog);
        return toResponse(saved, t -> true, null, catalogNumbers.get(saved.getId()), catalogNumbers);
    }
    @Transactional
    public ChallengeResponse update(Long id, ChallengeRequest request) {
        Challenge c = findChallenge(id);
        var prereqs = resolveAndValidatePrerequisites(c.getId(), c.getChallengeNumber(), request.prerequisiteChallengeNumbers());
        c.update(request.title(), request.description(), request.language(), request.vulnerabilityType(),
                request.vulnerabilitySubtype(), request.cwe(), request.starterCode(), request.difficulty());
        c.replaceTestCases(toEntities(request.testCases()));
        c.replacePrerequisites(prereqs);
        Challenge saved = challengeRepository.save(c);
        var catalog = challengeRepository.findByActiveTrueOrderByChallengeNumberAsc();
        var catalogNumbers = catalogNumbers(catalog);
        return toResponse(saved, t -> true, null, catalogNumbers.get(saved.getId()), catalogNumbers);
    }
    @Transactional
    public void delete(Long id) { challengeRepository.delete(findChallenge(id)); }

    private java.util.Set<Challenge> resolveAndValidatePrerequisites(Long currentChallengeId, Long currentChallengeNumber, List<Long> prerequisiteNumbers) {
        if (prerequisiteNumbers == null || prerequisiteNumbers.isEmpty()) {
            return java.util.Collections.emptySet();
        }
        java.util.Set<Challenge> resolved = new java.util.LinkedHashSet<>();
        for (Long number : prerequisiteNumbers) {
            if (number == null) continue;
            if (currentChallengeNumber != null && number.equals(currentChallengeNumber)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A challenge cannot have itself as a prerequisite (Challenge No. " + number + ")");
            }
            Challenge prereq = challengeRepository.findByChallengeNumber(number)
                    .or(() -> challengeRepository.findById(number))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prerequisite challenge not found: " + number));
            if (currentChallengeId != null && prereq.getId().equals(currentChallengeId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A challenge cannot have itself as a prerequisite (Challenge No. " + number + ")");
            }
            if (currentChallengeId != null && hasPrerequisiteCycle(prereq, currentChallengeId, new java.util.HashSet<>())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Circular prerequisite dependency detected with Challenge No. " + prereq.getChallengeNumber() + " (" + prereq.getTitle() + ")");
            }
            resolved.add(prereq);
        }
        return resolved;
    }

    private boolean hasPrerequisiteCycle(Challenge current, Long targetId, java.util.Set<Long> visited) {
        if (current.getId().equals(targetId)) return true;
        if (!visited.add(current.getId())) return false;
        for (Challenge next : current.getPrerequisites()) {
            if (hasPrerequisiteCycle(next, targetId, visited)) return true;
        }
        return false;
    }

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
    private java.util.Map<Long, Integer> catalogNumbers(List<Challenge> challenges) {
        java.util.Map<Long, Integer> numbers = new java.util.HashMap<>();
        for (int index = 0; index < challenges.size(); index++) numbers.put(challenges.get(index).getId(), index + 1);
        return numbers;
    }
    private ChallengeResponse toResponse(Challenge c, Predicate<TestCase> include, User user, Integer catalogNumber, java.util.Map<Long, Integer> catalogNumbers) {
        var access = user == null ? new com.securecode.ai.dto.PrerequisiteAccessResponse(c.getId(), true,
                c.getPrerequisites().stream().map(p -> new PrerequisiteStatusResponse(p.getId(), p.getChallengeNumber(), catalogNumbers.get(p.getId()), p.getTitle(), p.getDifficulty().name(), false, true)).toList())
                : prerequisiteAccess.access(user, c, catalogNumbers);
        boolean locked = !access.accessible();
        List<TestCaseResponse> tests = c.getTestCases().stream().filter(include)
                .sorted(Comparator.comparingInt(TestCase::getPosition))
                .map(t -> new TestCaseResponse(t.getId(), t.getInputData(), t.getExpectedOutput(), t.isHidden(), t.getPosition())).toList();
        int attempts = user == null ? 0 : (int) submissionRepository.countByUser_IdAndChallenge_Id(user.getId(), c.getId());
        List<com.securecode.ai.entity.Submission> submissions = user == null ? List.of()
                : submissionRepository.findByUser_EmailAndChallenge_IdOrderByAttemptNumberAsc(user.getEmail(), c.getId());
        Predicate<com.securecode.ai.entity.Submission> successful = submission -> submission.getLearningScoreState() == null
                ? submission.getLearningScore() >= 90 : submission.isVerifiedCompleted();
        boolean solved = submissions.stream().anyMatch(successful);
        boolean mastered = submissions.stream().anyMatch(com.securecode.ai.entity.Submission::isVerifiedMastered);
        Long successfulSubmissionId = submissions.stream().filter(successful)
                .max(Comparator.comparingInt(com.securecode.ai.entity.Submission::getAttemptNumber))
                .map(com.securecode.ai.entity.Submission::getId).orElse(null);
        return new ChallengeResponse(c.getId(), c.getChallengeNumber(), catalogNumber, c.getTitle(), c.getDescription(), c.getLanguage(), c.getVulnerabilityType(),
                c.getVulnerabilitySubtype(), c.getCwe(), c.getStarterCode(), tests, c.getDifficulty().name(), attempts, Math.max(0, 5 - attempts), solved, mastered,
                successfulSubmissionId, locked, access.prerequisites());
    }
}
