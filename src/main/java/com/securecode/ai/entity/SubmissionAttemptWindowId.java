package com.securecode.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record SubmissionAttemptWindowId(@Column(name = "user_id") Long userId,
        @Column(name = "challenge_id") Long challengeId) implements Serializable { }
