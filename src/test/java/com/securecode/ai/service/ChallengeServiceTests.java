package com.securecode.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.securecode.ai.dto.ChallengeRequest;
import com.securecode.ai.dto.ChallengeResponse;
import com.securecode.ai.dto.TestCaseRequest;
import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.Difficulty;
import com.securecode.ai.entity.Role;
import com.securecode.ai.entity.User;
import com.securecode.ai.repository.ChallengeRepository;
import com.securecode.ai.repository.SubmissionRepository;
import com.securecode.ai.repository.UserRepository;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ChallengeServiceTests {
    private ChallengeRepository challenges;
    private SubmissionRepository submissions;
    private UserRepository users;
    private PrerequisiteAccessService accessService;
    private ChallengeService service;

    @BeforeEach
    void setUp() {
        challenges = mock(ChallengeRepository.class);
        submissions = mock(SubmissionRepository.class);
        users = mock(UserRepository.class);
        accessService = mock(PrerequisiteAccessService.class);
        service = new ChallengeService(challenges, submissions, users, accessService);
    }

    @Test
    void createsChallengeWithPrerequisites() throws Exception {
        Challenge prereq = createChallengeEntity(1L, 101L, "Prereq Challenge");
        when(challenges.nextChallengeNumber()).thenReturn(102L);
        when(challenges.findByChallengeNumber(101L)).thenReturn(Optional.of(prereq));
        when(challenges.save(any(Challenge.class))).thenAnswer(invocation -> {
            Challenge arg = invocation.getArgument(0);
            setId(arg, 2L);
            return arg;
        });

        ChallengeRequest request = new ChallengeRequest(
                "New Challenge", "Description", "java", "Injection", "SQL Injection", "CWE-89",
                "class Main {}", List.of(new TestCaseRequest("input", "output", false, 0)),
                Difficulty.MEDIUM, List.of(101L));

        ChallengeResponse response = service.create(request);

        assertThat(response.title()).isEqualTo("New Challenge");
        assertThat(response.challengeNumber()).isEqualTo(102L);
        verify(challenges).save(any(Challenge.class));
    }

    @Test
    void updatesChallengeAndReplacesPrerequisites() throws Exception {
        Challenge existing = createChallengeEntity(2L, 102L, "Existing Challenge");
        Challenge prereq = createChallengeEntity(1L, 101L, "Prereq Challenge");

        when(challenges.findById(2L)).thenReturn(Optional.of(existing));
        when(challenges.findByChallengeNumber(101L)).thenReturn(Optional.of(prereq));
        when(challenges.save(existing)).thenReturn(existing);

        ChallengeRequest request = new ChallengeRequest(
                "Updated Challenge", "New Description", "java", "Injection", "SQL Injection", "CWE-89",
                "class Main {}", List.of(), Difficulty.EASY, List.of(101L));

        ChallengeResponse response = service.update(2L, request);

        assertThat(response.title()).isEqualTo("Updated Challenge");
        assertThat(existing.getPrerequisites()).containsExactly(prereq);
    }

    @Test
    void rejectsSelfReferencingPrerequisite() throws Exception {
        Challenge existing = createChallengeEntity(2L, 102L, "Existing Challenge");
        when(challenges.findById(2L)).thenReturn(Optional.of(existing));
        when(challenges.findByChallengeNumber(102L)).thenReturn(Optional.of(existing));

        ChallengeRequest request = new ChallengeRequest(
                "Updated Challenge", "Description", "java", "Injection", "SQL", "CWE-89",
                "class Main {}", List.of(), Difficulty.MEDIUM, List.of(102L));

        assertThatThrownBy(() -> service.update(2L, request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejectsCircularPrerequisiteDependency() throws Exception {
        Challenge a = createChallengeEntity(1L, 101L, "Challenge A");
        Challenge b = createChallengeEntity(2L, 102L, "Challenge B");

        // B already requires A
        b.replacePrerequisites(java.util.Set.of(a));

        when(challenges.findById(1L)).thenReturn(Optional.of(a));
        when(challenges.findByChallengeNumber(102L)).thenReturn(Optional.of(b));

        // Now trying to make A require B -> Cycle!
        ChallengeRequest request = new ChallengeRequest(
                "Challenge A", "Description", "java", "Injection", "SQL", "CWE-89",
                "class Main {}", List.of(), Difficulty.MEDIUM, List.of(102L));

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejectsNonExistentPrerequisiteChallenge() {
        when(challenges.nextChallengeNumber()).thenReturn(200L);
        when(challenges.findByChallengeNumber(999L)).thenReturn(Optional.empty());
        when(challenges.findById(999L)).thenReturn(Optional.empty());

        ChallengeRequest request = new ChallengeRequest(
                "New Challenge", "Description", "java", "Injection", "SQL", "CWE-89",
                "class Main {}", List.of(), Difficulty.MEDIUM, List.of(999L));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private Challenge createChallengeEntity(Long id, Long challengeNumber, String title) throws Exception {
        Challenge c = new Challenge(challengeNumber, title, "desc", "java", "type", "subtype", "cwe", "starter", Difficulty.MEDIUM);
        setId(c, id);
        return c;
    }

    private void setId(Object target, Long id) throws Exception {
        Field idField = target.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(target, id);
    }
}
