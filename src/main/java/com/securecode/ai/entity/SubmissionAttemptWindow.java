package com.securecode.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "submission_attempt_windows")
public class SubmissionAttemptWindow {
    @EmbeddedId private SubmissionAttemptWindowId id;
    @Column(name = "started_at", nullable = false) private Instant startedAt;
    @Column(name = "attempts_used", nullable = false) private int attemptsUsed;
    protected SubmissionAttemptWindow() { }
    public SubmissionAttemptWindow(Long userId, Long challengeId, Instant startedAt) { this.id = new SubmissionAttemptWindowId(userId, challengeId); this.startedAt = startedAt; }
    public Instant getStartedAt() { return startedAt; }
    public int getAttemptsUsed() { return attemptsUsed; }
    public void reset(Instant now) { startedAt = now; attemptsUsed = 0; }
    public void consume() { attemptsUsed++; }
}
