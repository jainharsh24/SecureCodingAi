package com.securecode.ai.service;

import com.securecode.ai.entity.SubmissionAttemptWindow;
import com.securecode.ai.entity.SubmissionAttemptWindowId;
import com.securecode.ai.entity.User;
import com.securecode.ai.repository.SubmissionAttemptWindowRepository;
import com.securecode.ai.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** The only authority for the persisted five-attempt, seven-day allowance. */
@Service
public class AttemptWindowService {
    private static final int LIMIT = 5;
    private static final Duration WINDOW = Duration.ofDays(7);
    private final SubmissionAttemptWindowRepository windows;
    private final UserRepository users;
    public AttemptWindowService(SubmissionAttemptWindowRepository windows, UserRepository users) { this.windows = windows; this.users = users; }
    @Transactional
    public int consume(User user, Long challengeId) {
        Instant now = Instant.now();
        // Serializes a user's first-window creation as well as updates to an existing window.
        users.findWithLockById(user.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        SubmissionAttemptWindowId id = new SubmissionAttemptWindowId(user.getId(), challengeId);
        SubmissionAttemptWindow window = windows.findWithLockById(id).orElseGet(() -> windows.saveAndFlush(new SubmissionAttemptWindow(user.getId(), challengeId, now)));
        if (!now.isBefore(window.getStartedAt().plus(WINDOW))) window.reset(now);
        if (window.getAttemptsUsed() >= LIMIT) throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Attempt limit reached. This challenge unlocks at " + window.getStartedAt().plus(WINDOW));
        window.consume();
        return window.getAttemptsUsed();
    }
}
