package com.securecode.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "submissions")
public class Submission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne(optional = false) @JoinColumn(name = "challenge_id", nullable = false) private Challenge challenge;
    @Column(nullable = false) private String language;
    @Column(name = "source_code", nullable = false) private String sourceCode;
    @Column(name = "attempt_number", nullable = false) private int attemptNumber;
    @Column(name = "learning_score", nullable = false) private int learningScore;
    @Column(name = "raw_assessment") private String rawAssessment;
    @Column(name = "submitted_at", insertable = false, updatable = false) private Instant submittedAt;
    protected Submission() { }
    public Submission(User user, Challenge challenge, String sourceCode, int attemptNumber) {
        this.user = user; this.challenge = challenge; this.language = challenge.getLanguage(); this.sourceCode = sourceCode; this.attemptNumber = attemptNumber;
    }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getLanguage() { return language; }
    public String getSourceCode() { return sourceCode; }
    public Challenge getChallenge() { return challenge; }
    public int getAttemptNumber() { return attemptNumber; }
    public int getLearningScore() { return learningScore; }
    public String getRawAssessment() { return rawAssessment; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void recordAssessment(int score, String rawAssessment) { this.learningScore = score; this.rawAssessment = rawAssessment; }
}
