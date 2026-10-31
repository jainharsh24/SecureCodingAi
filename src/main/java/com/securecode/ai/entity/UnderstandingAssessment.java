package com.securecode.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/** An immutable, individual attempt at explaining a challenge's security concept. */
@Entity
@Table(name = "understanding_assessments")
public class UnderstandingAssessment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne(optional = false) @JoinColumn(name = "challenge_id", nullable = false) private Challenge challenge;
    @ManyToOne @JoinColumn(name = "submission_id") private Submission submission;
    @Column(nullable = false, columnDefinition = "TEXT") private String question;
    @Column(nullable = false, columnDefinition = "TEXT") private String answer;
    @Column(name = "root_cause_points", nullable = false) private int rootCausePoints;
    @Column(name = "mechanism_points", nullable = false) private int mechanismPoints;
    @Column(name = "prevention_points", nullable = false) private int preventionPoints;
    @Column(nullable = false) private int score;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private UnderstandingAssessmentStatus status;
    @Column(name = "root_cause_feedback", columnDefinition = "TEXT") private String rootCauseFeedback;
    @Column(name = "mechanism_feedback", columnDefinition = "TEXT") private String mechanismFeedback;
    @Column(name = "prevention_feedback", columnDefinition = "TEXT") private String preventionFeedback;
    @Column(name = "unavailable_reason", columnDefinition = "TEXT") private String unavailableReason;
    @Column(name = "created_at", insertable = false, updatable = false) private Instant createdAt;
    protected UnderstandingAssessment() { }

    public UnderstandingAssessment(User user, Challenge challenge, String question, String answer, int rootCausePoints,
            int mechanismPoints, int preventionPoints, UnderstandingAssessmentStatus status, String rootCauseFeedback,
            String mechanismFeedback, String preventionFeedback, String unavailableReason) {
        this.user = user; this.challenge = challenge; this.question = question; this.answer = answer;
        this.rootCausePoints = rootCausePoints; this.mechanismPoints = mechanismPoints; this.preventionPoints = preventionPoints;
        this.score = rootCausePoints + mechanismPoints + preventionPoints; this.status = status;
        this.rootCauseFeedback = rootCauseFeedback; this.mechanismFeedback = mechanismFeedback;
        this.preventionFeedback = preventionFeedback; this.unavailableReason = unavailableReason;
    }
    public void associateWith(Submission submission) { this.submission = submission; }
    public Submission getSubmission() { return submission; }
    public Long getId() { return id; }
    public String getAnswer() { return answer; }
    public int getRootCausePoints() { return rootCausePoints; }
    public int getMechanismPoints() { return mechanismPoints; }
    public int getPreventionPoints() { return preventionPoints; }
    public int getScore() { return score; }
    public UnderstandingAssessmentStatus getStatus() { return status; }
    public String getRootCauseFeedback() { return rootCauseFeedback; }
    public String getMechanismFeedback() { return mechanismFeedback; }
    public String getPreventionFeedback() { return preventionFeedback; }
    public String getUnavailableReason() { return unavailableReason; }
}
