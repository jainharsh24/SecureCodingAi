package com.securecode.ai.service;

import com.securecode.ai.dto.PrerequisiteAccessResponse;
import com.securecode.ai.dto.PrerequisiteStatusResponse;
import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.Role;
import com.securecode.ai.entity.User;
import com.securecode.ai.repository.SubmissionRepository;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Uses the established persisted completion signal (learning score >= 90), never an attempt alone. */
@Service
public class PrerequisiteAccessService {
    private final SubmissionRepository submissions;
    public PrerequisiteAccessService(SubmissionRepository submissions) { this.submissions = submissions; }

    public PrerequisiteAccessResponse access(User user, Challenge target, Map<Long, Integer> catalogNumbers) {
        Set<Long> complete = submissions.findByUser_EmailOrderBySubmittedAtDesc(user.getEmail()).stream()
                .filter(submission -> submission.getLearningScore() >= 90)
                .map(submission -> submission.getChallenge().getId()).collect(Collectors.toSet());
        List<PrerequisiteStatusResponse> prerequisites = target.getPrerequisites().stream()
                .map(prerequisite -> status(prerequisite, complete, catalogNumbers, new HashSet<>(Set.of(target.getId()))) )
                .toList();
        return new PrerequisiteAccessResponse(target.getId(), user.getRole() == Role.ADMIN || prerequisites.stream().allMatch(PrerequisiteStatusResponse::completed), prerequisites);
    }

    public void requireAccess(User user, Challenge target, Map<Long, Integer> catalogNumbers) {
        if (!access(user, target, catalogNumbers).accessible()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Complete all prerequisites before accessing this challenge");
        }
    }

    private PrerequisiteStatusResponse status(Challenge challenge, Set<Long> complete, Map<Long, Integer> catalogNumbers, Set<Long> visiting) {
        boolean completed = complete.contains(challenge.getId());
        boolean accessible = completed || (visiting.add(challenge.getId()) && challenge.getPrerequisites().stream()
                .allMatch(next -> complete.contains(next.getId())));
        return new PrerequisiteStatusResponse(challenge.getId(), challenge.getChallengeNumber(), catalogNumbers.get(challenge.getId()),
                challenge.getTitle(), challenge.getDifficulty().name(), completed, accessible);
    }
}
