package com.securecode.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.Difficulty;
import com.securecode.ai.entity.HintAssistanceLevel;
import com.securecode.ai.entity.HintViewEvent;
import com.securecode.ai.entity.Role;
import com.securecode.ai.entity.User;
import com.securecode.ai.repository.ChallengeRepository;
import com.securecode.ai.repository.HintViewEventRepository;
import com.securecode.ai.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class HintServiceTests {
    @Test
    void repeatedViewOfTheSameHintDoesNotCreateAnotherEvent() {
        ChallengeRepository challenges = mock(ChallengeRepository.class);
        UserRepository users = mock(UserRepository.class);
        HintViewEventRepository views = mock(HintViewEventRepository.class);
        Challenge challenge = new Challenge(1, "SQL", "Use parameters", "java", "Injection", "SQL Injection", "CWE-89", "", Difficulty.EASY);
        User user = new User("student@example.com", "unused", Role.STUDENT);
        when(challenges.findById(1L)).thenReturn(Optional.of(challenge));
        when(users.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(views.findByUser_IdAndChallenge_IdAndAssistanceLevel(null, 1L, HintAssistanceLevel.CONCEPTUAL))
                .thenReturn(Optional.of(new HintViewEvent(user, challenge, HintAssistanceLevel.CONCEPTUAL)));

        var result = new HintService(challenges, users, views).view(1L, "student@example.com", HintAssistanceLevel.CONCEPTUAL);

        assertThat(result.level()).isEqualTo("CONCEPTUAL");
        verify(views, never()).save(any(HintViewEvent.class));
    }
}
