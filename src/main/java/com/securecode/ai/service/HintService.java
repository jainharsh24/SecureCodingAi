package com.securecode.ai.service;

import com.securecode.ai.dto.HintResponse;
import com.securecode.ai.dto.HintSummaryResponse;
import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.HintAssistanceLevel;
import com.securecode.ai.entity.HintViewEvent;
import com.securecode.ai.entity.User;
import com.securecode.ai.repository.ChallengeRepository;
import com.securecode.ai.repository.HintViewEventRepository;
import com.securecode.ai.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class HintService {
    private final ChallengeRepository challenges;
    private final UserRepository users;
    private final HintViewEventRepository views;
    public HintService(ChallengeRepository challenges, UserRepository users, HintViewEventRepository views) {
        this.challenges = challenges; this.users = users; this.views = views;
    }
    @Transactional(readOnly = true)
    public List<HintSummaryResponse> available(Long challengeId, String email) {
        challenge(challengeId); user(email);
        return Arrays.stream(HintAssistanceLevel.values()).map(level -> new HintSummaryResponse(level.name(), title(level))).toList();
    }
    @Transactional
    public HintResponse view(Long challengeId, String email, HintAssistanceLevel level) {
        Challenge challenge = challenge(challengeId); User user = user(email);
        // A repeated click returns the same hint but does not create another view record.
        if (views.findByUser_IdAndChallenge_IdAndAssistanceLevel(user.getId(), challengeId, level).isEmpty()) {
            views.save(new HintViewEvent(user, challenge, level));
        }
        return new HintResponse(level.name(), title(level), content(challenge, level));
    }
    private String title(HintAssistanceLevel level) { return switch (level) {
        case CONCEPTUAL -> "Conceptual hint"; case IMPLEMENTATION -> "Implementation hint"; case SOLUTION -> "Solution reveal";
    }; }
    private String content(Challenge challenge, HintAssistanceLevel level) {
        String target = nonBlank(challenge.getVulnerabilitySubtype(), challenge.getVulnerabilityType(), "the target vulnerability");
        return switch (level) {
            case CONCEPTUAL -> "Identify where untrusted input crosses into a security-sensitive operation for " + target + ". Keep data separate from the operation's syntax or control structure.";
            case IMPLEMENTATION -> "Trace the untrusted input to its sensitive sink. Replace string construction or direct interpretation with the language or library's parameter, binding, allow-list, or safe API mechanism required for " + target + ".";
            case SOLUTION -> "Reference solution:\n" + nonBlank(challenge.getSecureReferenceCode(), "No reference solution is configured for this challenge.");
        };
    }
    private Challenge challenge(Long id) { return challenges.findById(id).filter(Challenge::isActive)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found")); }
    private User user(String email) { return users.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found")); }
    private String nonBlank(String... values) { for (String value : values) if (value != null && !value.isBlank()) return value; return "the target vulnerability"; }
}
