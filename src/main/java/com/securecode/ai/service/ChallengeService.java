package com.securecode.ai.service;

import com.securecode.ai.dto.ChallengeRequest;
import com.securecode.ai.dto.ChallengeResponse;
import com.securecode.ai.dto.TestCaseRequest;
import com.securecode.ai.dto.TestCaseResponse;
import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.TestCase;
import com.securecode.ai.repository.ChallengeRepository;
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
    public ChallengeService(ChallengeRepository challengeRepository) { this.challengeRepository = challengeRepository; }

    @Transactional(readOnly = true)
    public List<ChallengeResponse> getAllForStudent() {
        return challengeRepository.findAll().stream().map(c -> toResponse(c, t -> !t.isHidden())).toList();
    }
    @Transactional(readOnly = true)
    public ChallengeResponse getByIdForStudent(Long id) { return toResponse(findChallenge(id), t -> !t.isHidden()); }
    @Transactional
    public ChallengeResponse create(ChallengeRequest request) {
        Challenge c = new Challenge(request.title(), request.description(), request.language(), request.vulnerabilityType(),
                request.vulnerabilitySubtype(), request.cwe(), request.starterCode());
        c.replaceTestCases(toEntities(request.testCases()));
        return toResponse(challengeRepository.save(c), t -> true);
    }
    @Transactional
    public ChallengeResponse update(Long id, ChallengeRequest request) {
        Challenge c = findChallenge(id);
        c.update(request.title(), request.description(), request.language(), request.vulnerabilityType(),
                request.vulnerabilitySubtype(), request.cwe(), request.starterCode());
        c.replaceTestCases(toEntities(request.testCases()));
        return toResponse(c, t -> true);
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
    private ChallengeResponse toResponse(Challenge c, Predicate<TestCase> include) {
        List<TestCaseResponse> tests = c.getTestCases().stream().filter(include)
                .sorted(Comparator.comparingInt(TestCase::getPosition))
                .map(t -> new TestCaseResponse(t.getId(), t.getInputData(), t.getExpectedOutput(), t.isHidden(), t.getPosition())).toList();
        return new ChallengeResponse(c.getId(), c.getTitle(), c.getDescription(), c.getLanguage(), c.getVulnerabilityType(),
                c.getVulnerabilitySubtype(), c.getCwe(), c.getStarterCode(), tests);
    }
}
