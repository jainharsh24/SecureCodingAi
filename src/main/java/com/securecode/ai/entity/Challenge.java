package com.securecode.ai.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "challenges")
public class Challenge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String title;
    @Column(nullable = false) private String description;
    @Column(nullable = false) private String language;
    @Column(name = "vulnerability_type") private String vulnerabilityType;
    @Column(name = "vulnerability_subtype") private String vulnerabilitySubtype;
    private String cwe;
    @Column(name = "starter_code") private String starterCode;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Difficulty difficulty = Difficulty.MEDIUM;
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestCase> testCases = new ArrayList<>();

    protected Challenge() { }
    public Challenge(String title, String description, String language, String type, String subtype, String cwe, String starterCode, Difficulty difficulty) {
        update(title, description, language, type, subtype, cwe, starterCode, difficulty);
    }
    public void update(String title, String description, String language, String type, String subtype, String cwe, String starterCode, Difficulty difficulty) {
        this.title = title; this.description = description; this.language = language; this.vulnerabilityType = type;
        this.vulnerabilitySubtype = subtype; this.cwe = cwe; this.starterCode = starterCode; this.difficulty = difficulty == null ? Difficulty.MEDIUM : difficulty;
    }
    public void replaceTestCases(List<TestCase> newTestCases) { testCases.clear(); newTestCases.forEach(this::addTestCase); }
    private void addTestCase(TestCase testCase) { testCase.setChallenge(this); testCases.add(testCase); }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLanguage() { return language; }
    public String getVulnerabilityType() { return vulnerabilityType; }
    public String getVulnerabilitySubtype() { return vulnerabilitySubtype; }
    public String getCwe() { return cwe; }
    public String getStarterCode() { return starterCode; }
    public Difficulty getDifficulty() { return difficulty; }
    public List<TestCase> getTestCases() { return List.copyOf(testCases); }
}
