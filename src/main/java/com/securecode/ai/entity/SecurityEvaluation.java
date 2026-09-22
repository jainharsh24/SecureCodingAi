package com.securecode.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "security_evaluations")
public class SecurityEvaluation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "submission_id", nullable = false) private Submission submission;
    @Column(name = "evaluator_name", nullable = false) private String evaluatorName;
    @Column(nullable = false) private boolean detected;
    @Column(name = "vulnerability_type") private String vulnerabilityType;
    private String cwe;
    private String severity;
    @Column(precision = 5, scale = 4) private BigDecimal confidence;
    private String evidence;
    protected SecurityEvaluation() { }
    public SecurityEvaluation(Submission submission, String evaluatorName, boolean detected, String vulnerabilityType,
            String cwe, String severity, BigDecimal confidence, String evidence) {
        this.submission = submission; this.evaluatorName = evaluatorName; this.detected = detected;
        this.vulnerabilityType = vulnerabilityType; this.cwe = cwe; this.severity = severity;
        this.confidence = confidence; this.evidence = evidence;
    }
    public String getEvaluatorName() { return evaluatorName; }
    public boolean isDetected() { return detected; }
    public String getVulnerabilityType() { return vulnerabilityType; }
    public String getCwe() { return cwe; }
    public String getSeverity() { return severity; }
}
