package com.securecode.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.securecode.ai.entity.Challenge;
import com.securecode.ai.entity.Difficulty;
import com.securecode.ai.entity.Role;
import com.securecode.ai.entity.Submission;
import com.securecode.ai.entity.User;
import com.securecode.ai.repository.SubmissionRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class PrerequisiteAccessServiceTests {
    @Test
    void locksUntilEveryPrerequisiteHasAnAuthoritativeCompletedSubmission() {
        SubmissionRepository submissions = mock(SubmissionRepository.class);
        User user = new User("student@example.test", "hash", Role.STUDENT);
        Challenge first = challenge(1L, "First", List.of());
        Challenge second = challenge(2L, "Second", List.of());
        Challenge target = challenge(3L, "Target", List.of(first, second));
        Submission firstComplete = completed(first);
        Submission secondFailed = failed(second);
        when(submissions.findByUser_EmailOrderBySubmittedAtDesc(user.getEmail())).thenReturn(List.of(firstComplete, secondFailed));
        PrerequisiteAccessService service = new PrerequisiteAccessService(submissions);

        var access = service.access(user, target, Map.of(1L, 1, 2L, 2, 3L, 3));
        assertThat(access.accessible()).isFalse();
        assertThat(access.prerequisites()).extracting(item -> item.completed()).containsExactly(true, false);
        assertThatThrownBy(() -> service.requireAccess(user, target, Map.of())).isInstanceOf(ResponseStatusException.class);

        Submission secondComplete = completed(second);
        when(submissions.findByUser_EmailOrderBySubmittedAtDesc(user.getEmail())).thenReturn(List.of(firstComplete, secondComplete));
        assertThat(service.access(user, target, Map.of()).accessible()).isTrue();
    }

    @Test
    void allowsChallengesWithoutPrerequisites() {
        SubmissionRepository submissions = mock(SubmissionRepository.class);
        User user = new User("student@example.test", "hash", Role.STUDENT);
        Challenge open = challenge(1L, "Open", List.of());
        when(submissions.findByUser_EmailOrderBySubmittedAtDesc(user.getEmail())).thenReturn(List.of());
        assertThat(new PrerequisiteAccessService(submissions).access(user, open, Map.of()).accessible()).isTrue();
    }

    private Challenge challenge(Long id, String title, List<Challenge> prerequisites) {
        Challenge challenge = mock(Challenge.class);
        when(challenge.getId()).thenReturn(id); when(challenge.getTitle()).thenReturn(title);
        when(challenge.getChallengeNumber()).thenReturn(id); when(challenge.getDifficulty()).thenReturn(Difficulty.BASIC);
        when(challenge.getPrerequisites()).thenReturn(prerequisites);
        return challenge;
    }
    private Submission completed(Challenge challenge) { Submission submission = mock(Submission.class); when(submission.getLearningScore()).thenReturn(100); when(submission.getChallenge()).thenReturn(challenge); return submission; }
    private Submission failed(Challenge challenge) { Submission submission = mock(Submission.class); when(submission.getLearningScore()).thenReturn(70); when(submission.getChallenge()).thenReturn(challenge); return submission; }
}
