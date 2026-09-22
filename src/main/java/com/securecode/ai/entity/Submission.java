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
@Table(name = "submissions")
public class Submission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne(optional = false) @JoinColumn(name = "challenge_id", nullable = false) private Challenge challenge;
    @Column(nullable = false) private String language;
    @Column(name = "source_code", nullable = false) private String sourceCode;
    protected Submission() { }
    public Submission(User user, Challenge challenge, String sourceCode) {
        this.user = user; this.challenge = challenge; this.language = challenge.getLanguage(); this.sourceCode = sourceCode;
    }
    public Long getId() { return id; }
    public String getLanguage() { return language; }
    public String getSourceCode() { return sourceCode; }
}
