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
    @Column(name = "security_score") private Integer securityScore;
    @Column(name = "security_correctness_status") private String securityCorrectnessStatus;
    @Column(name = "independent_problem_solving_score") private Integer independentProblemSolvingScore;
    @Column(name = "highest_hint_assistance") private String highestHintAssistance;
    @Column(name = "understanding_score") private Integer understandingScore;
    @Column(name = "understanding_assessment_status") private String understandingAssessmentStatus;
    @Column(name = "verified_final_score") private Integer verifiedFinalScore;
    @Column(name = "learning_score_state") private String learningScoreState;
    @Column(name = "challenge_completed") private Boolean challengeCompleted;
    @Column(name = "challenge_mastered") private Boolean challengeMastered;
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
    public Integer getSecurityScore() { return securityScore; }
    public String getSecurityCorrectnessStatus() { return securityCorrectnessStatus; }
    public Integer getIndependentProblemSolvingScore() { return independentProblemSolvingScore; }
    public String getHighestHintAssistance() { return highestHintAssistance; }
    public Integer getUnderstandingScore() { return understandingScore; }
    public String getUnderstandingAssessmentStatus() { return understandingAssessmentStatus; }
    public Integer getVerifiedFinalScore() { return verifiedFinalScore; }
    public String getLearningScoreState() { return learningScoreState; }
    public Boolean getChallengeCompleted() { return challengeCompleted; }
    public Boolean getChallengeMastered() { return challengeMastered; }
    public String getRawAssessment() { return rawAssessment; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void recordAssessment(int score, String rawAssessment) { this.learningScore = score; this.rawAssessment = rawAssessment; }
    public void recordSecurityCorrectness(int score, String status) { this.securityScore = score; this.securityCorrectnessStatus = status; }
    public void recordIndependentProblemSolving(int score, String assistance) { this.independentProblemSolvingScore = score; this.highestHintAssistance = assistance; }
    public void recordUnderstanding(Integer score, String status) { this.understandingScore = score; this.understandingAssessmentStatus = status; }
    public void recordOverallLearningScore(int score, Integer verifiedScore, String state, boolean completed, boolean mastered) {
        this.learningScore = score; this.verifiedFinalScore = verifiedScore; this.learningScoreState = state;
        this.challengeCompleted = completed; this.challengeMastered = mastered;
    }

    /** Null is intentional for pre-Step-5 records, which must not be retroactively certified. */
    public boolean isVerifiedCompleted() { return Boolean.TRUE.equals(challengeCompleted); }
    public boolean isVerifiedMastered() { return Boolean.TRUE.equals(challengeMastered); }
}
