package com.securecode.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "functional_evaluations")
public class FunctionalEvaluation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(optional = false) @JoinColumn(name = "submission_id", nullable = false, unique = true) private Submission submission;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private FunctionalEvaluationStatus status;
    @Column(name = "passed_tests", nullable = false) private int passedTests;
    @Column(name = "total_tests", nullable = false) private int totalTests;
    @Column private String details;
    protected FunctionalEvaluation() { }
    public FunctionalEvaluation(Submission submission, FunctionalEvaluationStatus status, int passedTests, int totalTests, String details) {
        this.submission = submission; this.status = status; this.passedTests = passedTests; this.totalTests = totalTests; this.details = details;
    }
    public Long getSubmissionId() { return submission.getId(); }
    public FunctionalEvaluationStatus getStatus() { return status; }
    public int getPassedTests() { return passedTests; }
    public int getTotalTests() { return totalTests; }
}
