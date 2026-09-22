package com.securecode.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "test_cases")
public class TestCase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "challenge_id", nullable = false) private Challenge challenge;
    @Column(name = "input_data", nullable = false) private String inputData;
    @Column(name = "expected_output", nullable = false) private String expectedOutput;
    @Column(name = "is_hidden", nullable = false) private boolean hidden;
    @Column(nullable = false) private int position;
    protected TestCase() { }
    public TestCase(String inputData, String expectedOutput, boolean hidden, int position) {
        this.inputData = inputData; this.expectedOutput = expectedOutput; this.hidden = hidden; this.position = position;
    }
    void setChallenge(Challenge challenge) { this.challenge = challenge; }
    public Long getId() { return id; }
    public String getInputData() { return inputData; }
    public String getExpectedOutput() { return expectedOutput; }
    public boolean isHidden() { return hidden; }
    public int getPosition() { return position; }
}
