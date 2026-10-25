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
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/** A view event, not merely a hint made available in the interface. */
@Entity
@Table(name = "hint_view_events", uniqueConstraints = @UniqueConstraint(name = "uq_hint_view_user_challenge_level",
        columnNames = {"user_id", "challenge_id", "assistance_level"}))
public class HintViewEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne(optional = false) @JoinColumn(name = "challenge_id", nullable = false) private Challenge challenge;
    @Enumerated(EnumType.STRING) @Column(name = "assistance_level", nullable = false) private HintAssistanceLevel assistanceLevel;
    @Column(name = "viewed_at", insertable = false, updatable = false) private Instant viewedAt;
    protected HintViewEvent() { }
    public HintViewEvent(User user, Challenge challenge, HintAssistanceLevel assistanceLevel) {
        this.user = user; this.challenge = challenge; this.assistanceLevel = assistanceLevel;
    }
    public HintAssistanceLevel getAssistanceLevel() { return assistanceLevel; }
}
